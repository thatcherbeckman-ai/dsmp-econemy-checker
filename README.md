# Economy Tracker — Fabric 1.21.11

Version **0.10.1** places auctions as **ground holograms**, anchored at your feet when you click Start, with a glowing item while bidding is open. It retains **payment-driven auctions** and an Overview welcome banner with your current Minecraft username and live character/skin. It retains the advanced Christmas dashboard.

The dashboard replaces the initial O-key menu with an advanced **Christmas dashboard**, inspired by the reference layouts: dark cards, purple sidebar navigation, clear ON/OFF controls, colored lights, snowy trees and presents. Snowfall can animate gently or remain still.

Press **O** or `/economy` to open it. The sidebar provides:

- **Overview**: a purple welcome banner with the logged-in username and actual local player model/skin, followed by observed balance, profit since reset, money in/out, a smooth balance-history chart, recent bets and top winners/losers. Empty or unknown readings remain empty; charts do not invent a zero starting balance.
- **HUD modules**: explicit ON/OFF controls for Balance, Profit, Player Nets, Recent Bets, Rakeback and Loans.
- **Appearance**: independent panel sizes, RGB colors, opacity and designs with live preview.
- **Text boxes**: the movable custom text and gradient editor.
- **HUD layout**: the drag editor, which now returns to the dashboard when closed.
- **Loans**: player/amount fields with Set, Add, Repay, Remove and No Loan list controls. These use the existing scoped local loan commands and save loan edits immediately. Cancel only reverts settings, not loan actions already saved.
- **Tracking**: automatic bet capture, passive balance reading, HUD rotation and the unpaid-stake settlement wait.
- **Settings**: Christmas background, snow animation, menu accent, shared HUD base scale and default glass opacity.
- **Auctions**: searchable item picker, quantity and timer, a world-anchored glowing item, cumulative bids and final winner.

**Save** or **Esc** saves preferences; **Cancel** restores the last saved checkpoint. Returning from a saved appearance/text editor updates that checkpoint so cancelling the dashboard cannot undo a child editor's committed changes. Failed settings saves keep the screen open and Cancel stays available. The overview and loan-table read API are read-only; changing preferences does not reset financial records. Layouts adapt to smaller GUI sizes, and loan-table pagination keeps every record accessible.

[Dashboard layout preview (sample data)](downloads/preview-0.9.0-overview.png) · [HUD settings layout preview](downloads/preview-0.9.0-modules.png). These images are native-free layout captures, not live Minecraft screenshots.

The existing features remain available, including freely positioned **custom text boxes** and a **Minecraft-glass pane design** inspired by the reference block: transparent tinted fill, white/cyan pixel edges and sparse diagonal highlights.

Press **O → Text boxes**. Click **New**, type the text, then drag its box anywhere in the canvas below the controls. Use literal `\n` in the input to create another line. Select existing boxes by clicking them or cycling the **Box** button.

Each box has:

- Independent size, position, left/center/right alignment, visibility and optional shadow.
- Inter, Inter bold or Minecraft font.
- **Start** and **End** font colors entered as six-digit RGB hex values (a leading `#` is accepted).
- **Grad: Solid / Across / Down**. Across blends character colors; Down renders clipped horizontal bands for a vertical gradient through the letters, including single-line text.
- A **Mint/Lime** reference preset (`49E8D4` → `55C900`) and Glass, plain Panel or off background.

**Done** or **Esc** saves; **Cancel** restores the text boxes from when the editor opened. Hidden boxes stay dimly visible for positioning in the editor but disappear from the in-game HUD. Text boxes remain visible independently of the rotating data tabs. Positions are saved relative to the viewport and remain within it when resolution changes. Up to 32 boxes, 1024 characters per box and 24 lines are supported.

Existing panels using the Glass design now use the pixel-edged **MC Glass** look. **O → Appearance → Design** offers MC Glass, Outline, Square, Minimal and **Soft glass** (the earlier rounded appearance). Custom background colors tint the glass. Text box frames default to glass and can be changed with their BG button.

The prior independent appearance settings are retained for **Profit, Player Nets, Recent Bets, Loans, Rakeback and Balance**. Press **O → Appearance**, then click **Edit: ...** to choose a panel. Each has its own:

- Size, adjustable from 50% to 200% of the existing global scale, with live preview and matching drag bounds.
- Accent, background, text, up/money and down/status colors. Click a color to open the RGB picker; use presets or adjust R/G/B in steps of 16 or 1. The header shows the exact hex value.
- Background opacity and MC Glass, Outline, Square, Minimal or Soft glass panel design.
- Reset button that restores only the selected panel's appearance.

**Done** or **Esc** saves and returns to the editor. **Cancel** restores the appearances from when the screen opened. A failed save keeps the screen open with an error message; Cancel remains available. Global layout, position, scale and glass controls are retained. Per-panel size is multiplied by the global scale and constrained to fit the viewport. Existing settings receive independent default appearances matching the original HUD. Appearance settings share the existing settings file; balances, loans, bets, profit and rakeback remain intact.

The prior **0.6.1 No Loan List** remains available. It adds a **No Loan List** section inside the Loans view. Add names with `/economy loan noloan add NAME`, remove them with `/economy loan noloan remove NAME`, or list them with `/economy loan noloan list [PAGE]`. Names are case insensitive, save with the active server/account, and appear with a red `NO LOAN` tag under a pink section heading. A No Loan list by itself keeps the Loans view available.

Listed players cannot have new or increased loan entries recorded in the tracker. Existing debts are retained and can still be reduced, repaid or removed; clearing a debt does not clear the No Loan flag. Removing a flag permits new entries again. This applies to local loan records, not payments made on the server.

The manually managed **Loans** chart is retained. It shows player names, outstanding amounts, total outstanding money and a page indicator, using the existing rounded panel and font. Loans joins Profit → Player Nets → Recent Bets → Loans in the same eight-second rotation with a 400 ms fade. Press **P** to advance the view. Loans is skipped when empty, and **O → HUD modules → Loans** toggles its visibility. Position, scale and glass settings use the shared rotating box.

Replace the previous mod JAR with `downloads/economy-tracker-0.10.1-mc1.21.11.jar`. Use Fabric Loader 0.18.4+, Fabric API for 1.21.11 and Java 21. Keep one Economy Tracker JAR installed.

## Auctions

Press **O → Auctions**. Search for an item by its name or identifier and click **Search**, select it, set **Quantity** (1–64) and **Timer** in seconds (1–86400), stand where you want the auction and click **Start**. Its location and dimension are captured at that moment. **Done** returns to the dashboard; close settings and walk a few blocks away to see the item sitting near the ground at that spot. Moving or turning does not move the hologram. Items come from Minecraft's live registry, including registered items from installed mods.

The display uses a **client-only dropped item**, with vanilla bobbing and rotation, plus a camera-facing text label above it. The item has a **glowing outline while the auction is RUNNING**; finishing, cancelling or interrupting bidding turns the glow off. It cannot be picked up, despawn normally, transfer money or take an inventory item. The item is pinned against local physics drift. The normal auction screen overlay is removed; the settings screen keeps its small setup preview.

While the timer runs, confirmed incoming payment receipts add to that player's **cumulative bid**. The leading player's name and total replace **No bids yet**. Names are case insensitive; equal totals favor the first player to reach that total. Receipts at the exact end time or later are outside the auction. At zero, **Winner: NAME** and their total stay above the ground item until **Hide result** or a new auction. With no receipts it says **No bids received**. **End early** settles the current bids; **Cancel lot** stops the auction without declaring a winner. Bids are listed by total in the setup screen where space permits.

Auction receipts still adjust the passive wallet and balance history, but do **not** create gambling bets, player nets, profit or rakeback entries, including with automatic bet capture disabled. The custom bet parser is also bypassed for these receipts. Payments outside a running auction retain their existing interpretation. All incoming receipts during a running auction are treated as bids, so finish the auction before accepting unrelated payments.

The hologram is visible **only in your own client**. A shared hologram visible to other players would require server support. It renders within 64 blocks in the saved dimension, disappears locally when you leave that area or dimension, and reappears at its saved location when you return. Hiding the result, disconnecting or changing accounts removes owned hologram entities.

This is a local tracker/display. Deliver the item and handle refunds yourself. Pause **Tracking → Automatic bets** before issuing auction refunds, since ordinary outgoing payments keep their existing bet interpretation. No payments, messages or server commands are sent by the auction feature.

The current lot, bids and starting location save atomically under `config/economy-tracker/auctions/`, isolated by server and account. Starting a new lot replaces that account's previous lot. Auction actions save immediately and are not undone by the dashboard's settings Cancel. Disconnecting or loading an unfinished lot marks it **Interrupted**, retaining recorded bids without declaring a winner after missed offline payments. A read failure preserves the original file and pauses edits. A bid-save failure stops the auction and shows an error; receipts during its original timer window remain excluded from gambling records. A new auction requires joining the configured server with writable tracker data. Older saved lots without a location retain their bids but do not get a guessed hologram position; start a new lot to place one.

The Overview banner renders your actual local player entity with Minecraft's inventory player renderer, using your currently loaded skin and the logged-in session's username. It uses no third-party skin service and never substitutes the reference player's character. When no in-world player exists, it shows the account name without a character model. The banner adapts to smaller GUI sizes.

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

The build compiles retained sources against the checksum-pinned 0.10.0 release. A guarded ASM adapter updates the world hologram on client ticks and removes its owned entities at the existing scope-cleanup hooks. The existing auction/payment guards, countdown and custom-parser bypass remain in place. A narrowly scoped Fabric access widener exposes only five display-entity setters for camera-facing text and label appearance. The dashboard, item picker, world item/text display and live player welcome banner compile against narrow API fixtures checked against Yarn 1.21.11 mappings. Fixtures and sample-data render harnesses remain excluded from the mod JAR.

Native-free checks execute location capture/persistence, stationary world entities, active-only glow, bob/rotation age independent of despawn, dimension/distance behavior, owned-ID cleanup, no auction HUD overlay, and cumulative bidding, exact deadlines, ties, empty results, overflow/validation, persistence, corruption and write failures, interruption, payment/bet isolation, native adapter hooks, item selection, winner rendering calls, the current-user player model, navigation and resizing. The existing financial ledger, wallet and loan model/store classes remain byte-for-byte unchanged. Payment capture adds auction receipt routing; ordinary payment and tax behavior retains regression coverage.

`DashboardPreview` in the fixture harness exports explicitly synthetic draw captures. `python3 tools/render_dashboard_preview.py` renders them with Pillow and the bundled Inter fonts for layout inspection.

Requires Java 21 (`JAVA_HOME`), JUnit Console Standalone (`JUNIT_JAR`), Gson (`GSON_JAR`), ASM 9.8 (`ASM_JAR`), ASM Tree 9.8 (`ASM_TREE_JAR`), Brigadier 1.3.10 (`BRIGADIER_JAR`) and JOML 1.10.8 (`JOML_JAR`). Defaults use the prepared workspace dependency cache.

**95 regression tests** cover winter preferences, settings transactions/checkpoints, independent HUD toggles, responsive geometry, bounded animation, balance interpolation without fabricated activity, text editing and persistence, Unicode measurement, drag bounds and viewport fitting, gradient endpoints, glass geometry and tinting, independent six-panel appearances, RGB adjustments, settings migration/persistence, validation, preview isolation, scale/geometry, fade colors, cancel/reset behavior, payment/tax behavior, balance reconciliation, bulk rakeback reset, balanced Player Nets, manual loans, repayments, No Loan list restrictions and repayment exceptions, schema migration, mixed-section pagination, persistence and server/account isolation, invalid input and file handling, failed saves, pagination, settings migration, and four-view rotation/fading. Seven adapter smoke suites exercise command scope guards, persistence, rendering calls, unsigned loan amounts, editor layout/toggling, save failures, and world-hologram lifecycle/glow. These are headless checks; they do not connect to a live DonutSMP account.

The hologram's native method/field selectors, protected client glow flag, overridable render-age getter, and all five private access-widener targets were additionally verified against the SHA-checked official Minecraft **1.21.11** client JAR and Fabric intermediary mappings. Re-run with cached official inputs:

```sh
python3 tools/verify_hologram_api.py --client /path/to/client.jar --mappings /path/to/mappings.tiny --version-info /path/to/version.json
```

These checks do not replace live in-game rendering validation.
