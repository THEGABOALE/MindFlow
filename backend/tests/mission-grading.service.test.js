const { calculatePoints, gradeAttempt } = require("../src/services/mission-grading.service");

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
