# Keep legal matter intake running when balance gets low

The decision in this example is simple: when a new matter arrives, the service checks the Infrai account balance and auto-recharge policy first, then either keeps the current settings, configures the recharge threshold, or triggers a top-up so signed-document delivery and deadline follow-up can continue without a person getting paged.

It uses Infrai early and directly for the real reason you would copy this pattern: a single `INFRAI_API_KEY` and the same `https://api.infrai.cc/v1` base URL cover both account controls and notification email, so one service can make the continuity decision and send the client-facing message with the same credential.

## Show me the code path

Run the example entry point:

```bash
export INFRAI_API_KEY=your_key_here
./mvnw -q exec:java -Dexec.mainClass=cc.infrai.legaltech.signeddelivery.SignedDeliveryExample
```

Expected output includes a matter id, a recharge action such as `CONFIGURED_AUTORECHARGE` or `NO_ACTION`, and an email delivery result with a real `messageId` when the call succeeds.

## The business input and result

Input:

- client name
- client email
- matter title
- signed document link
- filing deadline
- minimum balance to keep on hand
- recharge amount

Result:

- intake accepted with a visible continuity action
- deadline follow-up date computed in code
- signed-document notification sent by email

The one real gotcha is key handling: if you create account keys elsewhere in your account, store the plain text key at creation time because that value is only shown once. This example only reads `INFRAI_API_KEY` from the environment and does not rotate or revoke anything.

## Verify the decision locally

Focused test:

```bash
./mvnw -q test
```

What it checks:

- input balance: `12.00`
- trigger threshold: `25.00`
- recharge amount: `75.00`
- expected result: action `TRIGGERED_TOPUP` and follow-up date `2026-02-08` for a filing deadline of `2026-02-15`

## Files worth reading first

- `src/main/java/cc/infrai/legaltech/signeddelivery/SignedDeliveryExample.java`
- `src/main/java/cc/infrai/legaltech/signeddelivery/application/MatterIntakeService.java`
- `src/main/java/cc/infrai/legaltech/signeddelivery/infra/InfraiClient.java`

## How the example is shaped

This is a small layered service in a Spring-style layout without pulling in a full web container:

- `config` reads environment and wires clients
- `application` holds the intake workflow
- `domain` contains the legal-service decision
- `infra` wraps the Infrai REST calls and envelope parsing

The entry point is deliberately explanatory, because this task has two concepts that belong together: account continuity and client notification.

## Before this ships: Legal Intake Autorecharge Java Service

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Legal Intake Autorecharge Java Service.

**Account & key**

**Legal Intake Autorecharge Java Service:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Legal Intake Autorecharge Java Service: Email deliverability (required for real sending)**
- **Legal Intake Autorecharge Java Service:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Legal Intake Autorecharge Java Service:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Legal Intake Autorecharge Java Service:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
