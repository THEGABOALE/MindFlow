const { isFilledString, parseNonNegativeInt } = require("../src/utils/validation");

describe("isFilledString", () => {
  test("acepta texto con contenido", () => {
    expect(isFilledString("NOVA123")).toBe(true);
    expect(isFilledString("  a ")).toBe(true);
  });

  test("rechaza vacio, espacios y lo que no es texto", () => {
    [undefined, null, "", "   ", 123, {}, ["a"]].forEach((value) => {
      expect(isFilledString(value)).toBe(false);
    });
  });
});

describe("parseNonNegativeInt", () => {
  test("sin valor devuelve undefined para usar el valor por defecto", () => {
    expect(parseNonNegativeInt(undefined)).toBeUndefined();
    expect(parseNonNegativeInt("")).toBeUndefined();
  });

  test("lee enteros >= 0", () => {
    expect(parseNonNegativeInt("0")).toBe(0);
    expect(parseNonNegativeInt("25")).toBe(25);
  });

  test("negativos, decimales, texto o listas son invalidos", () => {
    ["-1", "1.5", "abc", "10abc", ["1", "2"]].forEach((raw) => {
      expect(parseNonNegativeInt(raw)).toBeNull();
    });
  });
});
