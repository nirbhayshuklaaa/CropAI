const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_farmer_1";
const BOB_UID = "bob_farmer_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profiles or soil reports", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("soil_reports").get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob Kisan",
      preferredLanguage: "hi"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    name: "Alice Farmer",
    preferredLanguage: "en",
    state: "Uttar Pradesh",
    district: "Varanasi"
  }));

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and query their own soil reports", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("soil_reports").doc("rep1").set({
    id: "rep1",
    userId: ALICE_UID,
    soilCategory: "Alluvial Loam",
    source: "LAB_TESTED",
    nitrogen: 78,
    phosphorus: 32,
    potassium: 45,
    ph: 6.8
  }));

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("soil_reports").doc("rep1").get());
});

test("Unauthenticated user: can read public crops and products catalog", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("crops").doc("rice").set({
      cropId: "rice",
      nameEn: "Rice",
      nameHi: "धान"
    });
    await context.firestore().collection("products").doc("p1").set({
      id: "p1",
      nameEn: "Seeds",
      priceInr: 500
    });
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("crops").doc("rice").get());
  await assertSucceeds(unauthDb.collection("products").doc("p1").get());
});
