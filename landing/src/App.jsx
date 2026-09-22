import novaLogo from "./assets/nova-logo-horizontal.png";
import novaIcon from "./assets/nova-icon.png";

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
      <div className="absolute -left-8 -top-8 h-20 w-20 text-[#82368c]/20 nova-float">
        <NovaSpark />
      </div>

      <div className="absolute -bottom-10 -right-6 h-28 w-28 text-[#1600b5]/15 nova-float-reverse">
        <NovaDoodle />
      </div>

      <div className="relative overflow-hidden rounded-[2.5rem] border-2 border-[#82368c]/15 bg-white p-5 sm:p-6">
        <div className="flex items-start justify-between gap-5">
          <div>
            <p className="eyebrow text-[#82368c]">
              Tu aventura
            </p>

            <h3 className="font-display mt-2 text-3xl text-[#25172a]">
              Mi ruta de aprendizaje
            </h3>
          </div>

          <span className="rounded-full bg-[#82368c]/10 px-4 py-2 text-xs font-bold text-[#82368c]">
            3 de 5
          </span>
        </div>

        <div className="mt-6 rounded-[1.8rem] bg-[#82368c]/7 p-5">
          <div className="flex items-center justify-between gap-4 text-xs font-bold text-[#82368c]">
            <span>Aprender para convivir</span>
            <span>60%</span>
          </div>

          <div className="mt-4 h-2 overflow-hidden rounded-full bg-[#82368c]/15">
            <div className="h-full w-3/5 rounded-full bg-[#1600b5]" />
          </div>

          <div className="mt-5 grid gap-3 sm:grid-cols-3">
            {missions.map((mission) => (
              <article
                key={mission.number}
                className={`reveal-card rounded-[1.4rem] border-2 p-4 ${
                  mission.active
                    ? "border-[#1600b5] bg-[#1600b5] text-white"
                    : "border-[#82368c]/10 bg-white text-[#25172a]"
                }`}
              >
                <div className="flex items-center justify-between text-xs font-bold">
                  <span>{mission.number}</span>

                  <span
                    className={
                      mission.active
                        ? "text-white"
                        : "text-[#82368c]"
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

        <div className="mt-4 flex items-center gap-4 rounded-[1.5rem] border-2 border-[#82368c]/10 p-4">
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-[#82368c] text-xl text-white">
            <img
              src={novaIcon}
              alt=""
              className="h-full w-full object-contain"
            />
          </div>

          <div>
            <p className="font-display text-lg text-[#25172a]">
              ¡Nueva misión!
            </p>

            <p className="mt-1 text-sm text-[#82368c]">
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
    <main className="min-h-screen bg-[#f7f3f8] text-[#25172a]">
      {/* NAVBAR */}
      <header className="sticky top-0 z-50 border-b border-[#82368c]/10 bg-[#f7f3f8]/95 backdrop-blur">
        <div className="mx-auto flex w-full max-w-[88rem] items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
          <a href="#inicio" aria-label="Ir al inicio">
            <img
              src={novaLogo}
              alt="NOVA Aplicación Educativa"
              className="h-12 w-auto"
            />
          </a>

          <nav className="hidden items-center gap-8 text-sm font-bold text-[#82368c] md:flex">
            <a
              href="#aventura"
              className="transition hover:text-[#1600b5]"
            >
              Tu aventura
            </a>

            <a
              href="#descubre"
              className="transition hover:text-[#1600b5]"
            >
              Descubre NOVA
            </a>

            <a
              href="#zafiro"
              className="transition hover:text-[#1600b5]"
            >
              Conoce a Zafiro
            </a>
          </nav>

          <a
            href="#comenzar"
            className="rounded-full bg-[#82368c] px-5 py-3 text-sm font-bold text-white transition hover:-translate-y-0.5 hover:bg-[#1600b5]"
          >
            Conocer NOVA
          </a>
        </div>
      </header>

      {/* HERO */}
      <section id="inicio" className="relative overflow-hidden">
        <div className="absolute right-4 top-12 h-20 w-20 text-[#82368c]/15 nova-float">
          <NovaSpark />
        </div>

        <div className="mx-auto grid w-full max-w-[88rem] items-center gap-10 px-4 py-12 sm:px-6 lg:grid-cols-[1fr_1fr] lg:gap-14 lg:px-8 lg:py-16">
          <div>
            <p className="eyebrow mb-5 text-[#82368c]">
              Aprende · juega · descubre
            </p>

            <h1 className="font-display max-w-3xl text-6xl leading-[0.96] text-[#25172a] sm:text-7xl lg:text-[5.5rem]">
              Cada reto puede enseñarte{" "}
              <span className="text-[#82368c]">
                algo para la vida.
              </span>
            </h1>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-[#503755] sm:text-[1.3rem]">
              Explora misiones, supera desafíos y aprende sobre igualdad,
              respeto y dignidad mientras avanzas junto a Zafiro.
            </p>

            <div className="mt-8 flex flex-col items-start gap-4 sm:flex-row">
              <a
                href="#aventura"
                className="rounded-full bg-[#82368c] px-8 py-4 font-bold text-white transition hover:-translate-y-1 hover:bg-[#1600b5]"
              >
                Comenzar la aventura
              </a>

              <a
                href="#descubre"
                className="rounded-full border-2 border-[#82368c] px-8 py-4 font-bold text-[#82368c] transition hover:bg-[#82368c] hover:text-white"
              >
                Descubrir NOVA
              </a>
            </div>

            <div className="mt-7 flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-full bg-[#1600b5] text-white">
                ✦
              </span>

              <p className="text-sm font-bold text-[#82368c]">
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
            <p className="eyebrow text-[#82368c]">
              Así se vive NOVA
            </p>

            <h2 className="font-display mt-4 text-5xl leading-none text-[#25172a] sm:text-6xl lg:text-[4.2rem]">
              Una misión.
              <br />
              Un reto.
              <br />
              Algo nuevo que aprender.
            </h2>

            <div className="mt-8 h-24 w-full max-w-xs text-[#82368c]/25">
              <NovaDoodle />
            </div>
          </div>

          <div className="divide-y-2 divide-[#82368c]/10 border-y-2 border-[#82368c]/10">
            {steps.map((step) => (
              <article
                key={step.number}
                className="grid gap-4 py-6 sm:grid-cols-[70px_0.8fr_1fr] sm:items-start sm:gap-6"
              >
                <span className="font-display text-4xl text-[#82368c]">
                  {step.number}
                </span>

                <h3 className="font-display text-xl text-[#25172a]">
                  {step.title}
                </h3>

                <p className="leading-7 text-[#614666]">
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
            <p className="eyebrow text-[#82368c]">
              Descubre mientras avanzas
            </p>

            <h2 className="font-display mt-4 text-5xl leading-none text-[#25172a] sm:text-6xl lg:text-[4.2rem]">
              Aprender puede sentirse como superar una misión.
            </h2>

            <p className="mt-5 max-w-3xl text-lg leading-8 text-[#614666]">
              En NOVA cada actividad tiene un propósito: pensar, elegir,
              comprender y descubrir cómo nuestras decisiones también pueden
              transformar lo que nos rodea.
            </p>
          </div>

          <div className="feature-grid mt-10 grid overflow-hidden rounded-[2rem] border-2 border-[#82368c]/10 sm:grid-cols-2">
            {features.map((feature) => (
              <article
                key={feature.number}
                className="reveal-card border-b-2 border-[#82368c]/10 p-6 sm:border-b-0 sm:border-r-2 sm:p-7"
              >
                <span className="font-display text-3xl text-[#82368c]/60">
                  {feature.number}
                </span>

                <h3 className="font-display mt-5 text-2xl text-[#25172a]">
                  {feature.title}
                </h3>

                <p className="mt-3 leading-7 text-[#614666]">
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
        <div className="absolute right-6 top-8 h-24 w-24 text-[#82368c]/15 nova-pulse">
          <NovaSpark />
        </div>

        <div className="mx-auto grid w-full max-w-[88rem] items-center gap-10 px-4 sm:px-6 lg:grid-cols-2 lg:gap-16 lg:px-8">
          <div>
            <p className="eyebrow text-[#82368c]">
              Tu compañero de ruta
            </p>

            <h2 className="font-display mt-4 text-5xl leading-none text-[#25172a] sm:text-6xl lg:text-[4.2rem]">
              No tienes que recorrer el camino solo.
            </h2>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-[#614666]">
              Zafiro te acompaña durante las actividades, te orienta cuando
              aparece un nuevo reto y celebra contigo cada paso que completas.
            </p>
          </div>

          <div className="relative flex min-h-[320px] items-center justify-center rounded-[3rem] bg-[#82368c] px-7 py-10 text-center text-white">
            <div className="absolute left-10 top-10 h-14 w-14 text-white/20 nova-float">
              <NovaSpark />
            </div>

            <div>
              <div className="mx-auto flex h-28 w-28 items-center justify-center overflow-hidden rounded-[1.4rem] bg-[#82368c] text-5xl text-[#82368c]">
                <img
                  src={novaIcon}
                  alt=""
                  className="h-full w-full scale-[1.08] object-cover"
                />
              </div>

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
          <div className="mx-auto h-14 w-14 text-[#82368c] nova-pulse">
            <img
              src={novaIcon}
              alt=""
              className="h-6 w-6 object-contain"
            />
          </div>

          <p className="eyebrow mt-6 text-[#82368c]">
            NOVA
          </p>

          <h2 className="font-display mx-auto mt-4 max-w-5xl text-5xl leading-none text-[#25172a] sm:text-7xl">
            Aprende. Avanza. Transforma.
          </h2>

          <p className="mx-auto mt-6 max-w-3xl text-lg leading-8 text-[#614666]">
            Una experiencia creada para aprender jugando, reflexionar sobre
            situaciones reales y descubrir que cada decisión también puede
            enseñarnos algo.
          </p>

          <a
            href="mailto:hola@nova.edu"
            className="mt-8 inline-flex rounded-full bg-[#82368c] px-8 py-4 font-bold text-white transition hover:-translate-y-1 hover:bg-[#1600b5]"
          >
            Conocer más sobre NOVA
          </a>

          <p className="mt-4 text-sm text-[#82368c]">
            ¿Eres docente o representas una institución? Conversemos.
          </p>
        </div>
      </section>

      <footer className="border-t border-[#82368c]/10 px-4 py-6 sm:px-6 lg:px-8">
        <div className="mx-auto flex w-full max-w-[88rem] flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <img
            src={novaLogo}
            alt="NOVA"
            className="h-11 w-auto"
          />

          <p className="max-w-xl text-sm leading-6 text-[#614666]">
            Una experiencia educativa para aprender sobre igualdad,
            dignidad, respeto y derechos de una forma diferente.
          </p>
        </div>
      </footer>
    </main>
  );
}

export default App;