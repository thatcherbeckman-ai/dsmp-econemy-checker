# Economy Tracker — Fabric 1.21.11

Version **0.6.0** adds a manually managed **Loans** chart. It shows player names, outstanding amounts, total outstanding money and a page indicator, using the existing rounded panel and font. Loans joins Profit → Player Nets → Recent Bets → Loans in the same eight-second rotation with a 400 ms fade. Press **P** to advance the view. Loans is skipped when empty, and **O → Trackers → Loans** toggles its visibility. Position, scale and glass settings use the shared rotating box.

Replace the previous mod JAR with `downloads/economy-tracker-0.6.0-mc1.21.11.jar`. Use Fabric Loader 0.18.4+, Fabric API for 1.21.11 and Java 21. Keep one Economy Tracker JAR installed.

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

Names are case insensitive and support Java and dotted Bedrock names. Amounts accept decimals and K/M/B/T suffixes. Amounts must be positive; repayments cannot exceed what is owed. The chart sorts by largest outstanding loan and displays 14 players per page, with up to 1000 names stored. Records save on each successful edit in `config/economy-tracker/loans/`, isolated by the active server and account. Disconnecting clears the displayed session; reconnecting reloads its saved file. Invalid files are preserved and edits paused. Failed saves retain the previous displayed records. Loans never alter balance, Profit, Player Nets, rakeback, bets, or graph history.

## Retained features

- `/economy rakeback resetall` resets all rakeback, including winning surplus, for the active server/account. `/economy rakeback reset NAME` still resets one player. Other financial records remain intact.
- Player Nets hides zero totals and shows equal numbers up/down when both exist, up to seven each. If only one side exists, it shows up to seven available players.
- Payment parsing supports `Name paid you $1` and `You paid Name $1 (+$1 tax)`. The wallet deducts payment plus tax; player results and rakeback exclude server tax. Confirmed receipts also work when another mod bypasses the normal outgoing-command event.
- `/economy profit reset`, `/economy graph reset`, and `/economy player reset NAME` retain their separate reset baselines.
- The mod never sends server commands or payments. `/economy balance sync` enables passive reading and checks the sidebar locally. Type `/bal` yourself for a fresh exact starting balance, or set it with `/economy balance AMOUNT`. `/economy auto on` enables payment-based bet/rakeback capture and balance reading.

Observed commands alone never move money. Repeated confirmations correlated with a command are suppressed for one second; without server transaction IDs, identical independent receipts cannot always be distinguished from duplicate server messages. Missed historical payments cannot be reconstructed automatically. Existing financial data and layout remain compatible; the loan file has its own versioned schema.

## Build and verification

```sh
python3 tools/build.py
```

The build compiles the retained repair, command, display and loan sources against the checksum-pinned 0.5.5 release. The original financial ledger and payment/wallet implementation are retained. Guarded ASM adapters attach the loan command, open/close the correct loan session, add the fourth view, and add the editor toggle. Generated adapters run in headless client, renderer and editor fixtures with JVM verification enabled. Other release entries are checked byte-for-byte apart from explicitly compiled or adapted classes. Test fixtures are excluded from the mod JAR.

Requires Java 21 (`JAVA_HOME`), JUnit Console Standalone (`JUNIT_JAR`), Gson (`GSON_JAR`), ASM 9.8 (`ASM_JAR`), ASM Tree 9.8 (`ASM_TREE_JAR`), and Brigadier 1.3.10 (`BRIGADIER_JAR`). Defaults use the prepared workspace dependency cache.

**37 regression tests** cover payment/tax behavior, balance reconciliation, bulk rakeback reset, balanced Player Nets, manual loans, repayments, persistence and server/account isolation, invalid input and file handling, failed saves, pagination, settings migration, and four-view rotation/fading. The adapter smoke checks exercise command scope guards, persistence, rendering, unsigned loan amounts, editor layout/toggling, and save failure reporting. These are headless checks; they do not connect to a live DonutSMP account.
