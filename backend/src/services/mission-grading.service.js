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
// Cada repaso siguiente de la misma mision dentro de REVIEW_PAY_WINDOW_HOURS
// paga la mitad del anterior (50, 25, 13, 6...) hasta un minimo de 1: repasar
// siempre da algo, pero repetir la misma mision no es la forma de juntar
// semillas. Pasado ese tiempo sin repasarla, vuelve a pagar la mitad.
const REVIEW_FACTOR = 0.5;
const REVIEW_PAY_WINDOW_HOURS = 24;
const MIN_REVIEW_POINTS = 1;

// Margen sobre el limite de tiempo de la mision para la red y para lo que
// tarda la app entre abrir el intento y mostrar el reloj.
const TIME_LIMIT_GRACE_SECONDS = 15;

// Potenciador "+30 s": se compra con semillas de la cuenta, una vez por
// intento. Es caro a propósito, para que sea una decisión y no un reflejo.
const EXTRA_TIME_COST = 500;
const EXTRA_TIME_SECONDS = 30;
// La pausa (la app en segundo plano) es gratis, pero con tope: alcanza para
// atender algo y volver, no para dejar el reloj parado.
const MAX_PAUSE_SECONDS = 300;

/**
 * @param {object} args
 * @param {number} [args.paidReviewsNearby] cuantos repasos de esta mision ya
 *   cobraron dentro de la ventana; cada uno parte a la mitad lo que paga este.
 */
const calculatePoints = ({ pointsReward, wrongAnswers, maxPlumas, isReview, paidReviewsNearby = 0 }) => {
  const plumasLeft = maxPlumas - wrongAnswers;

  if (plumasLeft <= 0) {
    return 0;
  }

  const penalty = wrongAnswers / (maxPlumas + 1);
  const earned = pointsReward * (1 - penalty);

  if (!isReview) {
    return Math.round(earned);
  }

  const reviewPoints = earned * REVIEW_FACTOR ** (paidReviewsNearby + 1);

  return Math.max(MIN_REVIEW_POINTS, Math.round(reviewPoints));
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
// extraSeconds son los 30 s del potenciador, si se compro.
const exceededTimeLimit = ({ elapsedSeconds, timeLimitSeconds, extraSeconds = 0 }) =>
  timeLimitSeconds != null && elapsedSeconds > timeLimitSeconds + extraSeconds + TIME_LIMIT_GRACE_SECONDS;

// Segundos jugados de un intento del telefono: del inicio al fin, menos la
// pausa (entre 0 y el tope).
const playedSeconds = ({ startedAt, finishedAt, pausedSeconds = 0 }) =>
  (finishedAt.getTime() - startedAt.getTime()) / 1000 -
  Math.min(Math.max(pausedSeconds, 0), MAX_PAUSE_SECONDS);

/**
 * Si el "+30 s" se cobra: solo en una mision con reloj y si el saldo
 * alcanza. Si no alcanza, no cuenta: ni cobra ni suma tiempo.
 */
const extraTimePurchase = ({ usedExtraTime, timeLimitSeconds, balance }) =>
  usedExtraTime && timeLimitSeconds != null && balance >= EXTRA_TIME_COST
    ? { seedsSpent: EXTRA_TIME_COST, extraSeconds: EXTRA_TIME_SECONDS }
    : { seedsSpent: 0, extraSeconds: 0 };

/**
 * Resultado de un intento ya corregido: si se perdió (sin plumas o sin
 * tiempo), el puntaje, las plumas que quedan y las semillas que paga.
 */
const decideAttemptOutcome = ({
  correctAnswers, wrongAnswers, maxPlumas, pointsReward,
  timedOut, elapsedSeconds, timeLimitSeconds, isReview, paidReviewsNearby, extraSeconds = 0
}) => {
  const ranOutOfPlumas = wrongAnswers >= maxPlumas;
  // La app avisa timedOut cuando su reloj llega a cero, pero eso solo puede
  // adelantar el fallo: el servidor también compara con las horas.
  const ranOutOfTime = Boolean(timedOut) || exceededTimeLimit({ elapsedSeconds, timeLimitSeconds, extraSeconds });
  const failed = ranOutOfTime || ranOutOfPlumas;

  const totalAnswers = correctAnswers + wrongAnswers;

  return {
    failed,
    status: failed ? "failed" : "completed",
    score: totalAnswers > 0 ? Math.round((correctAnswers / totalAnswers) * 100) : 0,
    plumasLeft: Math.max(maxPlumas - wrongAnswers, 0),
    pointsEarned: failed
      ? 0
      : calculatePoints({ pointsReward, wrongAnswers, maxPlumas, isReview, paidReviewsNearby })
  };
};

module.exports = {
  EXTRA_TIME_COST,
  EXTRA_TIME_SECONDS,
  MAX_PAUSE_SECONDS,
  REVIEW_PAY_WINDOW_HOURS,
  calculatePoints,
  decideAttemptOutcome,
  exceededTimeLimit,
  extraTimePurchase,
  gradeAttempt,
  playedSeconds
};
