# Expense Tracker

En enkel webbapplikation för att registrera och kunna ha en överblick över sina utgifter.

## Vad applikationen gör

Du lägger till utgifter med beskrivning, belopp, kategori och datum. Applikationen visar en lista över alla utgifter och en summering per kategori samt en totalsumma. Du kan ta bort enskilda utgifter, och listan filtreras/summeras löpande.


## Köra applikationen lokalt

```bash
git clone https://github.com/CarolineLuther/expense-tracker.git
cd expense-tracker
./mvnw spring-boot:run
```

Öppna sedan `http://localhost:8080` i webbläsaren.

## Kör backend-testerna (enhets- och integrationstester):

```bash
./mvnw test
```

## Köra E2E-testerna lokalt

```bash
cd e2e
npm install
npx playwright test
```

Playwright startar automatiskt Spring Boot-appen på port 8080 innan testerna körs. Testerna körs serialiserade (inte parallellt), eftersom mina E2E-tester delar en och samma databas. Om flera tester kör samtidigt, kan de råka störa varandras data mitt i testet vilket hände mig till en början.

## CI/CD-arbetsflöde

Vid varje push eller pull request mot `main` körs en GitHub Actions-workflow (`.github/workflows/ci.yml`) som:

1. **Bygger** applikationen med Maven
2. **Kör enhetstester** (`*ServiceTest`)
3. **Kör integrationstester** (`*IntegrationTest`, mot en H2-databas)
4. **Deployar** automatiskt till Render.com (endast vid push till `main`, via en deploy hook som anropas från workflowen)

Playwright E2E-testerna ingår inte i pipelinen och körs manuellt/lokalt enligt instruktionerna ovan.

Applikationen paketeras som en Docker-image (se `Dockerfile`) som Render bygger och kör.

Jag har jobbat i feature-branches med pull requests först mot `dev` och slutligen mot `main`, där pipelinen körs och måste vara grön innan merge.

## Länk till applikation

https://expense-tracker-1x7o.onrender.com/

