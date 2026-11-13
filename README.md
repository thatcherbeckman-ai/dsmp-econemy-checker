# Economy Tracker — Fabric 1.21.11

Version 0.5.3 fixes DonutSMP confirmations such as:

```text
unprayin paid you $ 1
You paid unprayin $ 1 (+$ 1 tax)
```

The wallet adds incoming payments and subtracts outgoing payments **plus the stated tax**. Player Nets and rakeback count the money received by the player, excluding server tax. Rakeback remains net player losses since their rakeback reset; wins reduce progress, down to zero. Confirmed receipts can also be read when another client mod bypasses the normal command-observation event. Observed commands alone never move money. Repeated confirmations correlated with a command are suppressed for one second; without a server transaction ID, identical independent receipts cannot always be distinguished from server duplicates.

Replace the old JAR with `downloads/economy-tracker-0.5.3-mc1.21.11.jar`, using Fabric Loader 0.18.4+, Fabric API for 1.21.11 and Java 21. Keep only one Economy Tracker JAR installed. Press O for the editor.

Use `/economy auto on` to enable payment bet/rakeback capture and balance reading. If no exact starting balance has been read, **type `/bal` yourself once**, or set it locally with `/economy balance AMOUNT`. `/economy balance sync` only enables passive reading and inspects the sidebar. The mod never sends `/bal`, payments, or other server commands. Confirmed payments then adjust the balance automatically. Rounded sidebar readings cannot erase exact payment changes. If a message format is unsupported, `/economy balance debug` prints local reading diagnostics.

Existing records, balance history, reset baselines and layout remain compatible with 0.5.2. A receipt missed by an older version cannot be reconstructed retroactively; an exact balance reply corrects the current wallet.

## Build and verification

The environment restart retained the released JARs but not the earlier uncommitted source files. This repair therefore compiles the two affected core classes against the checksum-pinned 0.5.2 release. The build preserves every other class and resource byte-for-byte, checks that no command-sending API is present, and updates the version metadata. This intentionally leaves the Minecraft integration and ledger schema intact. Commit the repair source, tests and build tool along with the download to keep future repairs reproducible.

Run:

```sh
python3 tools/build.py
```

Requires Java 21 (`JAVA_HOME`), JUnit Console Standalone (`JUNIT_JAR`) and Gson (`GSON_JAR`). Defaults use the managed workspace's prepared Java/dependency cache. The build runs 16 regression tests covering taxed/untaxed payments, command-independent confirmations, duplicates associated with commands, negative/failed inputs, small payments against rounded balances, payout matching, rakeback reset behavior, disabled capture, initial balance, and persistence. It does not connect to a live DonutSMP account.
