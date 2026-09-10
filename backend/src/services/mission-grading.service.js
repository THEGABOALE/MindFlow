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
const REVIEW_FACTOR = 0.5;

const calculatePoints = ({ pointsReward, wrongAnswers, maxPlumas, isReview }) => {
  const plumasLeft = maxPlumas - wrongAnswers;

  if (plumasLeft <= 0) {
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

module.exports = {
  calculatePoints,
  gradeAttempt
};
