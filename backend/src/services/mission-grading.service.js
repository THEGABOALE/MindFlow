// Logica de correccion de un intento de mision, separada del controlador
// para poder testearla sin necesitar una base de datos real: recibe filas ya
// leidas de la base (preguntas, opciones, pares) y las respuestas que mando
// el cliente, y devuelve el resultado sin tocar nada por fuera.

// Semillas que gana el estudiante al terminar una mision.
//
// Cada pluma perdida descuenta 1/(max_plumas + 1) de la recompensa base, asi
// que con las 3 plumas de siempre queda: 0 errores 100%, 1 error 75%,
// 2 errores 50%, y al tercer error se pierde la mision y no gana nada.
// El repaso (volver a jugar una mision ya completada) da la mitad, para que
// repetir hasta hacerlo perfecto siga valiendo la pena sin regalar semillas.
// Y solo paga una vez cada REVIEW_PAY_WINDOW_HOURS por mision: repetir la
// misma mision diez veces seguidas no puede ser la forma de juntar semillas.
const REVIEW_FACTOR = 0.5;
const REVIEW_PAY_WINDOW_HOURS = 24;

// Margen sobre el limite de tiempo de la mision para la red y para lo que
// tarda la app entre abrir el intento y mostrar el reloj.
const TIME_LIMIT_GRACE_SECONDS = 15;

/**
 * @param {object} args
 * @param {boolean} [args.reviewAlreadyPaid] true si ya cobro un repaso de esta
 *   mision dentro de la ventana; entonces este repaso no paga.
 */
const calculatePoints = ({ pointsReward, wrongAnswers, maxPlumas, isReview, reviewAlreadyPaid = false }) => {
  const plumasLeft = maxPlumas - wrongAnswers;

  if (plumasLeft <= 0 || (isReview && reviewAlreadyPaid)) {
    return 0;
  }

  const penalty = wrongAnswers / (maxPlumas + 1);
  const earned = pointsReward * (1 - penalty);

  return Math.round(isReview ? earned * REVIEW_FACTOR : earned);
};

/**
 * Corrige las respuestas de un intento contra las preguntas/pares reales de
 * la mision, no contra lo que mande el cliente: cualquier pregunta o par sin
 * respuesta cuenta como fallo, las respuestas de mas (de otra mision) o
 * duplicadas se ignoran, y si llega mas de una para la misma pregunta o par
 * (reintentos en el minijuego de relacion de conceptos) gana la ultima: es
 * el intento final con el que se quedo el estudiante.
 *
 * @param {object} args
 * @param {Array<{id:number, question_type:string}>} args.questions
 * @param {Array<{id:number, question_id:number, is_correct:boolean}>} args.options
 * @param {Array<{id:number, question_id:number}>} args.pairs
 * @param {Array<{questionId:number|string, selectedOptionId?:number|string, pairId?:number|string, selectedPairId?:number|string}>} args.answers
 * @returns {{correctAnswers:number, wrongAnswers:number, answerRows: Array<{questionId:number, selectedOptionId:number|null, pairId:number|null, isCorrect:boolean}>}}
 */
const gradeAttempt = ({ questions, options, pairs, answers }) => {
  const optionsById = new Map(options.map((row) => [row.id, row]));
  const pairsById = new Map(pairs.map((row) => [row.id, row]));

  const answerByQuestionId = new Map();
  const answerByPairId = new Map();

  for (const answer of Array.isArray(answers) ? answers : []) {
    const questionId = Number(answer.questionId);

    if (answer.pairId != null) {
      const pairId = Number(answer.pairId);
      const pair = pairsById.get(pairId);

      if (pair && pair.question_id === questionId) {
        answerByPairId.set(pairId, answer);
      }
    } else if (answer.selectedOptionId != null) {
      answerByQuestionId.set(questionId, answer);
    }
  }

  let correctAnswers = 0;
  let wrongAnswers = 0;
  const answerRows = [];

  for (const question of questions) {
    if (question.question_type === "matching") {
      const questionPairs = pairs.filter((pair) => pair.question_id === question.id);

      for (const pair of questionPairs) {
        const answer = answerByPairId.get(pair.id);
        // Acierta si unio el termino con su propio par; sin respuesta cuenta como fallo.
        const isCorrect = answer != null && Number(answer.selectedPairId) === pair.id;

        if (isCorrect) {
          correctAnswers += 1;
        } else {
          wrongAnswers += 1;
        }

        answerRows.push({
          questionId: question.id,
          selectedOptionId: null,
          pairId: pair.id,
          isCorrect
        });
      }

      continue;
    }

    const answer = answerByQuestionId.get(question.id);
    const option = answer ? optionsById.get(Number(answer.selectedOptionId)) : null;
    const isValidOption = Boolean(option && option.question_id === question.id);
    const isCorrect = isValidOption && option.is_correct;

    if (isCorrect) {
      correctAnswers += 1;
    } else {
      wrongAnswers += 1;
    }

    answerRows.push({
      questionId: question.id,
      selectedOptionId: isValidOption ? option.id : null,
      pairId: null,
      isCorrect
    });
  }

  return { correctAnswers, wrongAnswers, answerRows };
};

// El limite de tiempo lo mide el servidor con su propio reloj (desde que se
// abrio el intento hasta que llega el cierre), no lo que diga la app.
const exceededTimeLimit = ({ elapsedSeconds, timeLimitSeconds }) =>
  timeLimitSeconds != null && elapsedSeconds > timeLimitSeconds + TIME_LIMIT_GRACE_SECONDS;

/**
 * Resultado de un intento ya corregido: si se perdió (sin plumas o sin
 * tiempo), el puntaje, las plumas que quedan y las semillas que paga.
 */
const decideAttemptOutcome = ({
  correctAnswers, wrongAnswers, maxPlumas, pointsReward,
  timedOut, elapsedSeconds, timeLimitSeconds, isReview, reviewAlreadyPaid
}) => {
  const ranOutOfPlumas = wrongAnswers >= maxPlumas;
  // La app avisa timedOut cuando su reloj llega a cero, pero eso solo puede
  // adelantar el fallo: el servidor también compara con las horas.
  const ranOutOfTime = Boolean(timedOut) || exceededTimeLimit({ elapsedSeconds, timeLimitSeconds });
  const failed = ranOutOfTime || ranOutOfPlumas;

  const totalAnswers = correctAnswers + wrongAnswers;

  return {
    failed,
    status: failed ? "failed" : "completed",
    score: totalAnswers > 0 ? Math.round((correctAnswers / totalAnswers) * 100) : 0,
    plumasLeft: Math.max(maxPlumas - wrongAnswers, 0),
    pointsEarned: failed
      ? 0
      : calculatePoints({ pointsReward, wrongAnswers, maxPlumas, isReview, reviewAlreadyPaid })
  };
};

module.exports = {
  REVIEW_PAY_WINDOW_HOURS,
  calculatePoints,
  decideAttemptOutcome,
  exceededTimeLimit,
  gradeAttempt
};
