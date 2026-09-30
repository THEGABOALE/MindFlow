const { withTransaction } = require("../database/transaction");
const groupRepository = require("../repositories/group.repository");
const userRepository = require("../repositories/user.repository");

// Matricula en una sala al estudiante que ya inició sesión. El código NO crea
// cuentas: primero la persona se loguea (Google o ID) y recién ahí usa el
// código que le dio su colegio. Así, si cierra sesión y vuelve a entrar, la
// matrícula sigue guardada y no tiene que poner el código de nuevo.
const joinGroupByCode = async (req, res) => {
  const { code } = req.body || {};

  if (!code) {
    return res.status(400).json({
      message: "El código del grupo es obligatorio",
      status: "ERROR"
    });
  }

  try {
    // La matrícula, el centro del estudiante y el uso del código se guardan
    // juntos o no se guarda nada.
    const outcome = await withTransaction(async (db) => {
      const accessCode = await groupRepository.findUsableAccessCode(db, code.trim().toUpperCase());

      if (!accessCode) {
        return {
          httpStatus: 404,
          body: {
            message: "El código no es válido, expiró o alcanzó su límite de usos",
            status: "ERROR"
          }
        };
      }

      const buildResponse = (alreadyEnrolled) => ({
        message: alreadyEnrolled
          ? "Ya estabas en esta sala"
          : "Te uniste a la sala exitosamente",
        status: "OK",
        alreadyEnrolled,
        group: {
          id: accessCode.group_id,
          name: accessCode.group_name,
          grade: accessCode.grade,
          section: accessCode.section,
          schoolYear: accessCode.school_year
        },
        level: {
          id: accessCode.level_id,
          name: accessCode.level_name,
          code: accessCode.level_code,
          description: accessCode.level_description
        }
      });

      // Si ya está en otra sala del mismo año lectivo, no se cambia solo: un
      // traslado de sección lo tiene que hacer el docente o el coordinador.
      const currentEnrollment = await groupRepository.findEnrollmentForYear(
        db,
        req.user.id,
        accessCode.school_year
      );

      if (currentEnrollment) {
        if (currentEnrollment.group_id === accessCode.group_id) {
          return { httpStatus: 200, body: buildResponse(true) };
        }

        return {
          httpStatus: 409,
          body: {
            message: `Ya pertenecés a la sala "${currentEnrollment.group_name}" este año. Pedile a tu docente que te traslade.`,
            status: "ERROR"
          }
        };
      }

      await groupRepository.enrollStudent(db, req.user.id, accessCode.group_id);

      // El estudiante queda ligado al centro de su sala si todavía no lo estaba.
      await userRepository.assignCenterIfMissing(db, req.user.id, accessCode.center_id);

      await groupRepository.incrementCodeUses(db, accessCode.code_id);

      return { httpStatus: 201, body: buildResponse(false) };
    });

    return res.status(outcome.httpStatus).json(outcome.body);
  } catch (error) {
    return res.status(500).json({
      message: "Error al unir estudiante al grupo",
      status: "ERROR",
      error: error.message
    });
  }
};

module.exports = {
  joinGroupByCode
};
