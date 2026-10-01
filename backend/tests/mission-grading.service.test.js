const {
  calculatePoints,
  decideAttemptOutcome,
  exceededTimeLimit,
  gradeAttempt
} = require("../src/services/mission-grading.service");

describe("calculatePoints", () => {
  test("0 errores da el 100% de la recompensa", () => {
    expect(calculatePoints({ pointsReward: 100, wrongAnswers: 0, maxPlumas: 3, isReview: false })).toBe(100);
  });

  test("1 error de 3 plumas descuenta 1/4", () => {
    expect(calculatePoints({ pointsReward: 100, wrongAnswers: 1, maxPlumas: 3, isReview: false })).toBe(75);
  });

  test("2 errores de 3 plumas descuenta la mitad", () => {
    expect(calculatePoints({ pointsReward: 100, wrongAnswers: 2, maxPlumas: 3, isReview: false })).toBe(50);
  });

  test("agotar las plumas no da puntos (mision perdida)", () => {
    expect(calculatePoints({ pointsReward: 100, wrongAnswers: 3, maxPlumas: 3, isReview: false })).toBe(0);
    expect(calculatePoints({ pointsReward: 100, wrongAnswers: 5, maxPlumas: 3, isReview: false })).toBe(0);
  });

  test("un repaso perfecto da la mitad de las semillas", () => {
    expect(calculatePoints({ pointsReward: 100, wrongAnswers: 0, maxPlumas: 3, isReview: true })).toBe(50);
  });

  test("cada repaso seguido de la misma mision paga la mitad del anterior, hasta 1", () => {
    const paid = [0, 1, 2, 3, 4, 5, 6, 7, 8].map((paidReviewsNearby) =>
      calculatePoints({ pointsReward: 100, wrongAnswers: 0, maxPlumas: 3, isReview: true, paidReviewsNearby })
    );

    expect(paid).toEqual([50, 25, 13, 6, 3, 2, 1, 1, 1]);
  });

  test("la baja tambien aplica sobre la recompensa con plumas perdidas", () => {
    const paid = [0, 1, 2].map((paidReviewsNearby) =>
      calculatePoints({ pointsReward: 200, wrongAnswers: 1, maxPlumas: 3, isReview: true, paidReviewsNearby })
    );

    expect(paid).toEqual([75, 38, 19]);
  });

  test("un repaso sin plumas no paga ni siquiera el minimo", () => {
    expect(
      calculatePoints({ pointsReward: 100, wrongAnswers: 3, maxPlumas: 3, isReview: true, paidReviewsNearby: 9 })
    ).toBe(0);
  });

  test("los repasos anteriores no afectan a una mision que no es repaso", () => {
    expect(
      calculatePoints({ pointsReward: 100, wrongAnswers: 0, maxPlumas: 3, isReview: false, paidReviewsNearby: 4 })
    ).toBe(100);
  });
});

describe("decideAttemptOutcome", () => {
  const base = {
    correctAnswers: 8, wrongAnswers: 0, maxPlumas: 3, pointsReward: 200,
    timedOut: false, elapsedSeconds: 30, timeLimitSeconds: null,
    isReview: false, paidReviewsNearby: 0
  };

  test("todo bien y a tiempo: completada con la recompensa completa", () => {
    expect(decideAttemptOutcome(base)).toEqual({
      failed: false, status: "completed", score: 100, plumasLeft: 3, pointsEarned: 200
    });
  });

  test("agotar las plumas la pierde y no paga", () => {
    const outcome = decideAttemptOutcome({ ...base, correctAnswers: 5, wrongAnswers: 3 });
    expect(outcome).toMatchObject({ failed: true, status: "failed", plumasLeft: 0, pointsEarned: 0, score: 63 });
  });

  test("si la app avisa que se acabó el tiempo, la pierde", () => {
    expect(decideAttemptOutcome({ ...base, timedOut: true })).toMatchObject({ failed: true, pointsEarned: 0 });
  });

  test("pasarse del límite más el margen la pierde aunque la app no avise", () => {
    expect(decideAttemptOutcome({ ...base, timeLimitSeconds: 45, elapsedSeconds: 61 })).toMatchObject({ failed: true });
    expect(decideAttemptOutcome({ ...base, timeLimitSeconds: 45, elapsedSeconds: 60 })).toMatchObject({ failed: false });
  });

  test("un repaso paga la mitad y el siguiente la mitad de eso", () => {
    expect(decideAttemptOutcome({ ...base, isReview: true }).pointsEarned).toBe(100);
    expect(decideAttemptOutcome({ ...base, isReview: true, paidReviewsNearby: 1 }).pointsEarned).toBe(50);
  });

  test("sin respuestas el puntaje es 0", () => {
    expect(decideAttemptOutcome({ ...base, correctAnswers: 0, wrongAnswers: 0 }).score).toBe(0);
  });
});

describe("exceededTimeLimit", () => {
  test("una mision sin limite de tiempo nunca se pasa", () => {
    expect(exceededTimeLimit({ elapsedSeconds: 99999, timeLimitSeconds: null })).toBe(false);
  });

  test("dentro del limite mas el margen de 15 s no se pasa", () => {
    expect(exceededTimeLimit({ elapsedSeconds: 45, timeLimitSeconds: 45 })).toBe(false);
    expect(exceededTimeLimit({ elapsedSeconds: 60, timeLimitSeconds: 45 })).toBe(false);
  });

  test("despues del margen se pasa aunque la app no lo diga", () => {
    expect(exceededTimeLimit({ elapsedSeconds: 60.5, timeLimitSeconds: 45 })).toBe(true);
    expect(exceededTimeLimit({ elapsedSeconds: 120, timeLimitSeconds: 45 })).toBe(true);
  });
});

describe("gradeAttempt - opcion multiple / verdadero-falso", () => {
  const questions = [
    { id: 1, question_type: "multiple_choice" },
    { id: 2, question_type: "multiple_choice" }
  ];

  const options = [
    { id: 10, question_id: 1, is_correct: true },
    { id: 11, question_id: 1, is_correct: false },
    { id: 20, question_id: 2, is_correct: true },
    { id: 21, question_id: 2, is_correct: false }
  ];

  test("responder todo bien da 100% de aciertos", () => {
    const answers = [
      { questionId: 1, selectedOptionId: 10 },
      { questionId: 2, selectedOptionId: 20 }
    ];

    const result = gradeAttempt({ questions, options, pairs: [], answers });

    expect(result.correctAnswers).toBe(2);
    expect(result.wrongAnswers).toBe(0);
  });

  test("omitir a proposito la pregunta que se iba a fallar no evita que cuente como fallo", () => {
    // El cliente manda solo la respuesta correcta y omite la otra pregunta entera.
    const answers = [{ questionId: 1, selectedOptionId: 10 }];

    const result = gradeAttempt({ questions, options, pairs: [], answers });

    expect(result.correctAnswers).toBe(1);
    expect(result.wrongAnswers).toBe(1);
  });

  test("una respuesta duplicada para la misma pregunta usa la ultima", () => {
    const answers = [
      { questionId: 1, selectedOptionId: 11 },
      { questionId: 1, selectedOptionId: 10 },
      { questionId: 2, selectedOptionId: 20 }
    ];

    const result = gradeAttempt({ questions, options, pairs: [], answers });

    expect(result.correctAnswers).toBe(2);
    expect(result.wrongAnswers).toBe(0);
  });

  test("una opcion que pertenece a otra pregunta se ignora, no aprueba por error", () => {
    const answers = [
      { questionId: 1, selectedOptionId: 20 }, // la opcion 20 es de la pregunta 2
      { questionId: 2, selectedOptionId: 20 }
    ];

    const result = gradeAttempt({ questions, options, pairs: [], answers });

    expect(result.correctAnswers).toBe(1);
    expect(result.wrongAnswers).toBe(1);
  });
});

describe("gradeAttempt - relacion de conceptos", () => {
  const questions = [{ id: 3, question_type: "matching" }];
  const pairs = [
    { id: 100, question_id: 3 },
    { id: 101, question_id: 3 },
    { id: 102, question_id: 3 }
  ];

  test("cada par sin responder cuenta como fallo", () => {
    const answers = [{ questionId: 3, pairId: 100, selectedPairId: 100 }];

    const result = gradeAttempt({ questions, options: [], pairs, answers });

    expect(result.correctAnswers).toBe(1);
    expect(result.wrongAnswers).toBe(2);
  });

  test("si el ultimo intento de un par fue el correcto, cuenta como acierto", () => {
    const answers = [
      { questionId: 3, pairId: 100, selectedPairId: 999 }, // primer intento, erroneo
      { questionId: 3, pairId: 100, selectedPairId: 100 }, // reintento, correcto
      { questionId: 3, pairId: 101, selectedPairId: 101 },
      { questionId: 3, pairId: 102, selectedPairId: 102 }
    ];

    const result = gradeAttempt({ questions, options: [], pairs, answers });

    expect(result.correctAnswers).toBe(3);
    expect(result.wrongAnswers).toBe(0);
  });

  test("un par que no pertenece a esta mision se ignora", () => {
    const answers = [{ questionId: 999, pairId: 555, selectedPairId: 555 }];

    const result = gradeAttempt({ questions, options: [], pairs, answers });

    // Los 3 pares reales quedan sin respuesta -> los 3 cuentan como fallo.
    expect(result.correctAnswers).toBe(0);
    expect(result.wrongAnswers).toBe(3);
  });
});
