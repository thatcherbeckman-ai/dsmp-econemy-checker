# Economy Tracker — Fabric 1.21.11

Version 0.5.4 balances the Player Nets display: when both sides exist, it shows the same number of players up and down, up to seven each. It chooses the largest gains and losses; positive and negative groups are each sorted from highest net to lowest. Zero-net players are hidden, including players whose results cancel out. When only one side has players, up to seven available players are shown. This changes only row selection, leaving ledger totals, bet history, reset baselines, balance and rakeback intact.

The 0.5.3 payment fixes are retained, including DonutSMP confirmations such as:

```text
unprayin paid you $ 1
You paid unprayin $ 1 (+$ 1 tax)
```

The wallet adds incoming payments and subtracts outgoing payments **plus the stated tax**. Player Nets and rakeback count the money received by the player, excluding server tax. Rakeback remains net player losses since their rakeback reset; wins reduce progress, down to zero. Confirmed receipts can also be read when another client mod bypasses the normal command-observation event. Observed commands alone never move money. Repeated confirmations correlated with a command are suppressed for one second; without a server transaction ID, identical independent receipts cannot always be distinguished from server duplicates.

Replace the old JAR with `downloads/economy-tracker-0.5.4-mc1.21.11.jar`, using Fabric Loader 0.18.4+, Fabric API for 1.21.11 and Java 21. Keep only one Economy Tracker JAR installed. Press O for the editor.

Use `/economy auto on` to enable payment bet/rakeback capture and balance reading. If no exact starting balance has been read, **type `/bal` yourself once**, or set it locally with `/economy balance AMOUNT`. `/economy balance sync` only enables passive reading and inspects the sidebar. The mod never sends `/bal`, payments, or other server commands. Confirmed payments then adjust the balance automatically. Rounded sidebar readings cannot erase exact payment changes. If a message format is unsupported, `/economy balance debug` prints local reading diagnostics.

Existing records, balance history, reset baselines and layout remain compatible with 0.5.2. A receipt missed by an older version cannot be reconstructed retroactively; an exact balance reply corrects the current wallet.

## Build and verification

The build compiles the available repair/display sources against the checksum-pinned 0.5.3 release. A guarded ASM transformation redirects exactly the two `Ledger.playerNets()` calls in the renderer's `nets` method to the new display selection helper, and updates that panel's empty-state message. The transformation validates the hook count and scope. Other rendering methods, Minecraft integration, resources, ledger schema and financial logic are retained. All other release entries are checked byte-for-byte, except the compiled wallet/payment repair classes whose sources remain included.

Run:

```sh
python3 tools/build.py
```

Requires Java 21 (`JAVA_HOME`), JUnit Console Standalone (`JUNIT_JAR`) Gson (`GSON_JAR`) and ASM 9.8 (`ASM_JAR`). Defaults use the managed workspace's prepared Java/dependency cache. The build runs 22 regression tests, including six Player Nets tests for matching counts, zero filtering, single-sided data, ordering and reset-baseline behavior. The 16 existing payment tests cover taxed/untaxed payments, command-independent confirmations, duplicates associated with commands, negative/failed inputs, small payments against rounded balances, payout matching, rakeback reset behavior, disabled capture, initial balance, and persistence. It does not connect to a live DonutSMP account.
