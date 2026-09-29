import novaLogo from "./assets/nova-logo.webp";
import novaIcon from "./assets/nova-icon.webp";

const steps = [
  {
    number: "01",
    title: "Entra a tu ruta",
    text: "Descubre misiones preparadas para tu nivel y empieza a avanzar.",
  },
  {
    number: "02",
    title: "Acepta el reto",
    text: "Responde, relaciona, decide y pon a prueba lo que sabes.",
  },
  {
    number: "03",
    title: "Aprende jugando",
    text: "Cada actividad te ayuda a comprender situaciones que también pasan en la vida real.",
  },
  {
    number: "04",
    title: "Mira cuánto avanzaste",
    text: "Completa misiones, desbloquea nuevos retos y observa tu progreso.",
  },
];

const features = [
  {
    number: "01",
    title: "Misiones",
    text: "Pequeños desafíos para avanzar paso a paso.",
  },
  {
    number: "02",
    title: "Retos interactivos",
    text: "Actividades diferentes para aprender haciendo.",
  },
  {
    number: "03",
    title: "Niveles",
    text: "Una ruta adaptada a cada etapa de aprendizaje.",
  },
  {
    number: "04",
    title: "Recompensas",
    text: "Celebra tus logros mientras completas la experiencia.",
  },
];

// Las imágenes ya vienen con fondo transparente y al tamaño en que se usan.
// Los originales en alta resolución están en landing/design/.
function NovaMark({ className = "" }) {
  return (
    <img
      src={novaIcon}
      alt=""
      width="256"
      height="256"
      loading="lazy"
      decoding="async"
      className={`nova-mark ${className}`}
    />
  );
}

function NovaLogo({ className = "" }) {
  return (
    <img
      src={novaLogo}
      alt="NOVA Aplicación Educativa"
      width="552"
      height="144"
      className={`nova-logo ${className}`}
    />
  );
}

function NovaSpark({ className = "" }) {
  return (
    <svg
      viewBox="0 0 80 80"
      aria-hidden="true"
      className={className}
    >
      <path
        d="M40 7C43 25 55 37 73 40C55 43 43 55 40 73C37 55 25 43 7 40C25 37 37 25 40 7Z"
        fill="currentColor"
      />
    </svg>
  );
}

function NovaDoodle() {
  return (
    <svg
      viewBox="0 0 320 150"
      aria-hidden="true"
      className="h-full w-full"
    >
      <path
        d="M18 105C61 35 111 134 156 68C193 15 249 23 300 86"
        fill="none"
        stroke="currentColor"
        strokeWidth="5"
        strokeLinecap="round"
        strokeDasharray="1 14"
      />

      <circle cx="40" cy="42" r="7" fill="currentColor" />
      <circle cx="272" cy="30" r="5" fill="currentColor" />

      <path
        d="M226 106C231 88 247 81 260 91C247 93 238 101 226 106Z"
        fill="currentColor"
      />
    </svg>
  );
}

function LearningPreview() {
  const missions = [
    {
      number: "01",
      title: "Conocernos",
      status: "¡Listo!",
      active: false,
    },
    {
      number: "02",
      title: "Escuchar",
      status: "¡Listo!",
      active: false,
    },
    {
      number: "03",
      title: "Ponernos en su lugar",
      status: "Ahora",
      active: true,
    },
  ];

  return (
    <div className="relative">
      <div className="absolute -left-8 -top-8 h-20 w-20 text-nova-purple/20 nova-float">
        <NovaSpark />
      </div>

      <div className="absolute -bottom-10 -right-6 h-28 w-28 text-nova-blue/15 nova-float-reverse">
        <NovaDoodle />
      </div>

      <div className="relative overflow-hidden rounded-[2.5rem] border-2 border-nova-purple/15 bg-white p-5 sm:p-6">
        <div className="flex items-start justify-between gap-5">
          <div>
            <p className="eyebrow text-nova-purple">
              Tu aventura
            </p>

            <h3 className="font-display mt-2 text-3xl text-nova-ink">
              Mi ruta de aprendizaje
            </h3>
          </div>

          <span className="rounded-full bg-nova-purple/10 px-4 py-2 text-xs font-bold text-nova-purple">
            3 de 5
          </span>
        </div>

        <div className="mt-6 rounded-[1.8rem] bg-nova-purple/7 p-5">
          <div className="flex items-center justify-between gap-4 text-xs font-bold text-nova-purple">
            <span>Aprender para convivir</span>
            <span>60%</span>
          </div>

          <div className="mt-4 h-2 overflow-hidden rounded-full bg-nova-purple/15">
            <div className="h-full w-3/5 rounded-full bg-nova-blue" />
          </div>

          <div className="mt-5 grid gap-3 sm:grid-cols-3">
            {missions.map((mission) => (
              <article
                key={mission.number}
                className={`reveal-card rounded-[1.4rem] border-2 p-4 ${
                  mission.active
                    ? "border-nova-blue bg-nova-blue text-white"
                    : "border-nova-purple/10 bg-white text-nova-ink"
                }`}
              >
                <div className="flex items-center justify-between text-xs font-bold">
                  <span>{mission.number}</span>

                  <span
                    className={
                      mission.active
                        ? "text-white"
                        : "text-nova-purple"
                    }
                  >
                    {mission.status}
                  </span>
                </div>

                <p className="font-display mt-6 text-lg leading-tight">
                  {mission.title}
                </p>
              </article>
            ))}
          </div>
        </div>

        <div className="mt-4 flex items-center gap-4 rounded-[1.5rem] border-2 border-nova-purple/10 p-4">
          <NovaMark className="h-12 w-12 shrink-0 text-nova-purple" />

          <div>
            <p className="font-display text-lg text-nova-ink">
              ¡Nueva misión!
            </p>

            <p className="mt-1 text-sm text-nova-purple">
              Practica la empatía y descubre otra forma de mirar una situación.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

function App() {
  return (
    <main className="min-h-screen bg-nova-mist text-nova-ink">
      {/* NAVBAR */}
      <header className="sticky top-0 z-50 border-b border-nova-purple/10 bg-nova-mist/95 backdrop-blur">
        <div className="mx-auto flex w-full max-w-[88rem] items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
          <a href="#inicio" aria-label="Ir al inicio">
            <NovaLogo />
          </a>

          <nav className="hidden items-center gap-8 text-sm font-bold text-nova-purple md:flex">
            <a
              href="#aventura"
              className="transition hover:text-nova-blue"
            >
              Tu aventura
            </a>

            <a
              href="#descubre"
              className="transition hover:text-nova-blue"
            >
              Descubre NOVA
            </a>

            <a
              href="#zafiro"
              className="transition hover:text-nova-blue"
            >
              Conoce a Zafiro
            </a>
          </nav>

          <a
            href="#comenzar"
            className="rounded-full bg-nova-purple px-5 py-3 text-sm font-bold text-white transition hover:-translate-y-0.5 hover:bg-nova-blue"
          >
            Conocer NOVA
          </a>
        </div>
      </header>

      {/* HERO */}
      <section id="inicio" className="relative overflow-hidden">
        <div className="absolute right-4 top-12 h-20 w-20 text-nova-purple/15 nova-float">
          <NovaSpark />
        </div>

        <div className="mx-auto grid w-full max-w-[88rem] items-center gap-10 px-4 py-12 sm:px-6 lg:grid-cols-[1fr_1fr] lg:gap-14 lg:px-8 lg:py-16">
          <div>
            <p className="eyebrow mb-5 text-nova-purple">
              Aprende · juega · descubre
            </p>

            <h1 className="font-display max-w-3xl text-6xl leading-[0.96] text-nova-ink sm:text-7xl lg:text-[5.5rem]">
              Cada reto puede enseñarte{" "}
              <span className="text-nova-purple">
                algo para la vida.
              </span>
            </h1>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-nova-body sm:text-[1.3rem]">
              Explora misiones, supera desafíos y aprende sobre igualdad,
              respeto y dignidad mientras avanzas junto a Zafiro.
            </p>

            <div className="mt-8 flex flex-col items-start gap-4 sm:flex-row">
              <a
                href="#aventura"
                className="rounded-full bg-nova-purple px-8 py-4 font-bold text-white transition hover:-translate-y-1 hover:bg-nova-blue"
              >
                Comenzar la aventura
              </a>

              <a
                href="#descubre"
                className="rounded-full border-2 border-nova-purple px-8 py-4 font-bold text-nova-purple transition hover:bg-nova-purple hover:text-white"
              >
                Descubrir NOVA
              </a>
            </div>

            <div className="mt-7 flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-full bg-nova-blue text-white">
                ✦
              </span>

              <p className="text-sm font-bold text-nova-purple">
                Una forma diferente de aprender, pensar y participar.
              </p>
            </div>
          </div>

          <LearningPreview />
        </div>
      </section>

      {/* CÓMO SE VIVE NOVA */}
      <section
        id="aventura"
        className="mx-auto w-full max-w-[88rem] px-4 py-14 sm:px-6 lg:px-8 lg:py-16"
      >
        <div className="grid gap-8 lg:grid-cols-[0.8fr_1.2fr] lg:gap-14">
          <div>
            <p className="eyebrow text-nova-purple">
              Así se vive NOVA
            </p>

            <h2 className="font-display mt-4 text-5xl leading-none text-nova-ink sm:text-6xl lg:text-[4.2rem]">
              Una misión.
              <br />
              Un reto.
              <br />
              Algo nuevo que aprender.
            </h2>

            <div className="mt-8 h-24 w-full max-w-xs text-nova-purple/25">
              <NovaDoodle />
            </div>
          </div>

          <div className="divide-y-2 divide-nova-purple/10 border-y-2 border-nova-purple/10">
            {steps.map((step) => (
              <article
                key={step.number}
                className="grid gap-4 py-6 sm:grid-cols-[70px_0.8fr_1fr] sm:items-start sm:gap-6"
              >
                <span className="font-display text-4xl text-nova-purple">
                  {step.number}
                </span>

                <h3 className="font-display text-xl text-nova-ink">
                  {step.title}
                </h3>

                <p className="leading-7 text-nova-muted">
                  {step.text}
                </p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* DESCUBRE */}
      <section id="descubre" className="py-14 sm:py-16">
        <div className="mx-auto w-full max-w-[88rem] px-4 sm:px-6 lg:px-8">
          <div className="max-w-4xl">
            <p className="eyebrow text-nova-purple">
              Descubre mientras avanzas
            </p>

            <h2 className="font-display mt-4 text-5xl leading-none text-nova-ink sm:text-6xl lg:text-[4.2rem]">
              Aprender puede sentirse como superar una misión.
            </h2>

            <p className="mt-5 max-w-3xl text-lg leading-8 text-nova-muted">
              En NOVA cada actividad tiene un propósito: pensar, elegir,
              comprender y descubrir cómo nuestras decisiones también pueden
              transformar lo que nos rodea.
            </p>
          </div>

          <div className="feature-grid mt-10 grid overflow-hidden rounded-[2rem] border-2 border-nova-purple/10 sm:grid-cols-2">
            {features.map((feature) => (
              <article
                key={feature.number}
                className="reveal-card border-b-2 border-nova-purple/10 p-6 sm:border-b-0 sm:border-r-2 sm:p-7"
              >
                <span className="font-display text-3xl text-nova-purple/60">
                  {feature.number}
                </span>

                <h3 className="font-display mt-5 text-2xl text-nova-ink">
                  {feature.title}
                </h3>

                <p className="mt-3 leading-7 text-nova-muted">
                  {feature.text}
                </p>
              </article>
            ))}
          </div>
        </div>
      </section>

      {/* ZAFIRO */}
      <section
        id="zafiro"
        className="relative overflow-hidden py-14 sm:py-16"
      >
        <div className="absolute right-6 top-8 h-24 w-24 text-nova-purple/15 nova-pulse">
          <NovaSpark />
        </div>

        <div className="mx-auto grid w-full max-w-[88rem] items-center gap-10 px-4 sm:px-6 lg:grid-cols-2 lg:gap-16 lg:px-8">
          <div>
            <p className="eyebrow text-nova-purple">
              Tu compañero de ruta
            </p>

            <h2 className="font-display mt-4 text-5xl leading-none text-nova-ink sm:text-6xl lg:text-[4.2rem]">
              No tienes que recorrer el camino solo.
            </h2>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-nova-muted">
              Zafiro te acompaña durante las actividades, te orienta cuando
              aparece un nuevo reto y celebra contigo cada paso que completas.
            </p>
          </div>

          <div className="relative flex min-h-[320px] items-center justify-center rounded-[3rem] bg-nova-purple px-7 py-10 text-center text-white">
            <div className="absolute left-10 top-10 h-14 w-14 text-white/20 nova-float">
              <NovaSpark />
            </div>

            <div>
              <NovaMark className="mx-auto h-28 w-28 text-white" />

              <p className="font-display mt-6 text-3xl">
                ¡Vamos, puedes hacerlo!
              </p>

              <p className="mx-auto mt-3 max-w-sm text-white/75">
                Cada misión superada es una nueva oportunidad para aprender.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CIERRE */}
      <section id="comenzar" className="py-14 sm:py-16">
        <div className="mx-auto w-full max-w-[72rem] px-4 text-center sm:px-6 lg:px-8">
          <div className="mx-auto h-14 w-14 text-nova-purple nova-pulse">
            <NovaMark className="h-14 w-14 text-nova-purple" />
          </div>

          <p className="eyebrow mt-6 text-nova-purple">
            NOVA
          </p>

          <h2 className="font-display mx-auto mt-4 max-w-5xl text-5xl leading-none text-nova-ink sm:text-7xl">
            Aprende. Avanza. Transforma.
          </h2>

          <p className="mx-auto mt-6 max-w-3xl text-lg leading-8 text-nova-muted">
            Una experiencia creada para aprender jugando, reflexionar sobre
            situaciones reales y descubrir que cada decisión también puede
            enseñarnos algo.
          </p>

          <a
            href="mailto:hola@nova.edu"
            className="mt-8 inline-flex rounded-full bg-nova-purple px-8 py-4 font-bold text-white transition hover:-translate-y-1 hover:bg-nova-blue"
          >
            Conocer más sobre NOVA
          </a>

          <p className="mt-4 text-sm text-nova-purple">
            ¿Eres docente o representas una institución? Conversemos.
          </p>
        </div>
      </section>

      <footer className="border-t border-nova-purple/10 px-4 py-6 sm:px-6 lg:px-8">
        <div className="mx-auto flex w-full max-w-[88rem] flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <NovaLogo />

          <p className="max-w-xl text-sm leading-6 text-nova-muted">
            Una experiencia educativa para aprender sobre igualdad,
            dignidad, respeto y derechos de una forma diferente.
          </p>
        </div>
      </footer>
    </main>
  );
}

export default App;