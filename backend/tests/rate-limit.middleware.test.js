const { loginAccountKey } = require("../src/middleware/rate-limit.middleware");

describe("loginAccountKey", () => {
  test("la misma cuenta desde IPs distintas comparte el mismo cupo", () => {
    const desdeUnaIp = loginAccountKey({ ip: "200.10.0.1", body: { loginId: "garciaga" } });
    const desdeOtraIp = loginAccountKey({ ip: "181.55.3.9", body: { loginId: "garciaga" } });

    expect(desdeUnaIp).toBe("account:garciaga");
    expect(desdeOtraIp).toBe(desdeUnaIp);
  });

  test("el ID se normaliza igual que en el login", () => {
    expect(loginAccountKey({ ip: "200.10.0.1", body: { loginId: "  GarciaGA " } })).toBe("account:garciaga");
  });

  test("cuentas distintas desde la misma IP no se mezclan", () => {
    const a = loginAccountKey({ ip: "200.10.0.1", body: { loginId: "garciaga" } });
    const b = loginAccountKey({ ip: "200.10.0.1", body: { loginId: "profedemo" } });

    expect(a).not.toBe(b);
  });

  test("sin ID se cuenta por IP", () => {
    expect(loginAccountKey({ ip: "200.10.0.1", body: {} })).toBe("ip:200.10.0.1");
    expect(loginAccountKey({ ip: "200.10.0.1", body: { loginId: 123 } })).toBe("ip:200.10.0.1");
  });
});
