# Economy Tracker — Fabric 1.21.11

Version **0.6.1** adds a **No Loan List** section inside the Loans view. Add names with `/economy loan noloan add NAME`, remove them with `/economy loan noloan remove NAME`, or list them with `/economy loan noloan list [PAGE]`. Names are case insensitive, save with the active server/account, and appear with a red `NO LOAN` tag under a pink section heading. A No Loan list by itself keeps the Loans view available.

Listed players cannot have new or increased loan entries recorded in the tracker. Existing debts are retained and can still be reduced, repaid or removed; clearing a debt does not clear the No Loan flag. Removing a flag permits new entries again. This applies to local loan records, not payments made on the server.

The manually managed **Loans** chart is retained. It shows player names, outstanding amounts, total outstanding money and a page indicator, using the existing rounded panel and font. Loans joins Profit → Player Nets → Recent Bets → Loans in the same eight-second rotation with a 400 ms fade. Press **P** to advance the view. Loans is skipped when empty, and **O → Trackers → Loans** toggles its visibility. Position, scale and glass settings use the shared rotating box.

Replace the previous mod JAR with `downloads/economy-tracker-0.6.1-mc1.21.11.jar`. Use Fabric Loader 0.18.4+, Fabric API for 1.21.11 and Java 21. Keep one Economy Tracker JAR installed.

## Loan commands

These are local records of money owed, kept separately from the payment/bet ledger. They do not transfer money or automatically classify payments as loan repayments.

| Command | Result |
| --- | --- |
| `/economy loan set Alex 10M` | Set Alex's total outstanding loan to $10M. |
| `/economy loan add Alex 2M` | Add $2M to Alex's outstanding loan. |
| `/economy loan paid Alex 5M` | Record a $5M repayment; a fully repaid loan disappears. |
| `/economy loan remove Alex` | Remove Alex's loan record. |
| `/economy loan list` | Show the current loan page in chat. |
| `/economy loan page 2` | Select page 2 in the chart and list it in chat. |
| `/economy loan noloan add Alex` | Add Alex to the No Loan List. |
| `/economy loan noloan remove Alex` | Remove Alex from the No Loan List. |
| `/economy loan noloan list` | List No Loan names in chat; append a page number if needed. |

Names are case insensitive and support Java and dotted Bedrock names. Amounts accept decimals and K/M/B/T suffixes. Amounts must be positive; repayments cannot exceed what is owed. The chart sorts outstanding loans by amount and No Loan names alphabetically. With both sections populated, it shows up to seven loan entries plus six No Loan names and their heading per page, so both sections appear together. A loan-only chart keeps 14 entries per page; a No Loan-only chart shows 13 names and a heading. `/economy loan page NUMBER` reaches further chart pages. Each list supports up to 1000 names. Records save on each successful edit in `config/economy-tracker/loans/`, isolated by the active server and account. Disconnecting clears the displayed session; reconnecting reloads its saved file. Invalid files are preserved and edits paused. Failed saves retain the previous displayed records. Loans never alter balance, Profit, Player Nets, rakeback, bets, or graph history.

## Retained features

- `/economy rakeback resetall` resets all rakeback, including winning surplus, for the active server/account. `/economy rakeback reset NAME` still resets one player. Other financial records remain intact.
- Player Nets hides zero totals and shows equal numbers up/down when both exist, up to seven each. If only one side exists, it shows up to seven available players.
- Payment parsing supports `Name paid you $1` and `You paid Name $1 (+$1 tax)`. The wallet deducts payment plus tax; player results and rakeback exclude server tax. Confirmed receipts also work when another mod bypasses the normal outgoing-command event.
- `/economy profit reset`, `/economy graph reset`, and `/economy player reset NAME` retain their separate reset baselines.
- The mod never sends server commands or payments. `/economy balance sync` enables passive reading and checks the sidebar locally. Type `/bal` yourself for a fresh exact starting balance, or set it with `/economy balance AMOUNT`. `/economy auto on` enables payment-based bet/rakeback capture and balance reading.

Observed commands alone never move money. Repeated confirmations correlated with a command are suppressed for one second; without server transaction IDs, identical independent receipts cannot always be distinguished from duplicate server messages. Missed historical payments cannot be reconstructed automatically. Existing financial data and layout remain compatible; loan file schema 1 is read and migrated to schema 2 on the next edit, retaining outstanding debts. Schema 2 stores the No Loan list alongside loans.

## Build and verification

```sh
python3 tools/build.py
```

The build compiles the retained sources against the checksum-pinned 0.6.0 release. A guarded ASM transformation changes only the Loans renderer's amount labels and row colors, so the section heading and No Loan tags do not appear as zero-dollar debts. The client, session/storage adapters, financial ledger, wallet, payments and editor remain byte-for-byte unchanged. Other entries are verified apart from explicitly compiled sources. Generated rendering and command adapters also run in headless fixtures with JVM verification enabled. Fixtures are excluded from the mod JAR.

Requires Java 21 (`JAVA_HOME`), JUnit Console Standalone (`JUNIT_JAR`), Gson (`GSON_JAR`), ASM 9.8 (`ASM_JAR`), ASM Tree 9.8 (`ASM_TREE_JAR`), and Brigadier 1.3.10 (`BRIGADIER_JAR`). Defaults use the prepared workspace dependency cache.

**46 regression tests** cover payment/tax behavior, balance reconciliation, bulk rakeback reset, balanced Player Nets, manual loans, repayments, No Loan list restrictions and repayment exceptions, schema migration, mixed-section pagination, persistence and server/account isolation, invalid input and file handling, failed saves, pagination, settings migration, and four-view rotation/fading. The adapter smoke checks exercise command scope guards, persistence, rendering, unsigned loan amounts, editor layout/toggling, and save failure reporting. These are headless checks; they do not connect to a live DonutSMP account.
