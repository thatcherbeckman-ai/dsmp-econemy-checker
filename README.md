# thatchacats econemy checker — Fabric 26.3 and 1.21.11

Version **0.13.1** makes inventory animations much fuller: **Low / Medium / High** now use 32 / 80 / 128 ambient decorations, with extra New Year confetti, more flakes behind the player model, and three falling speeds. Independent scatter fills the screen instead of placing decorations along a diagonal. Animation off still freezes the scene. The menu also gains evergreen garlands, hanging ornaments, a wreath, more wrapped presents and 96 extra snow particles. Existing Christmas background and snow-animation controls apply to the added decorations.

The existing **Inventory themes** section offers: original holiday palettes, themed slot frames and animated decorations for your survival inventory. It retains the cleaner 0.12.1 menus and the name **thatchacats econemy checker**.

[Inventory themes preview](downloads/preview-0.13.1-inventory-themes.png) — native Minecraft capture from the isolated offline test profile.

Press **O → Inventory themes**, choose a style, and click **Done**. The picker has a live preview, three pages of choices, animation on/off, Low/Medium/High effects, and speed from 25% to 200%. **Speed** resets to 100%. **Cancel** discards the draft; **Done** or **Esc** saves it. Vanilla is selected by default and restores your normal inventory/resource-pack backdrop.

Choices include **Christmas, Halloween, New Year, Valentine's Day, Lunar New Year, St. Patrick's Day, Easter, Eid, Diwali, Hanukkah, Thanksgiving, Independence Day, Birthday**, and **Spring, Summer, Autumn and Winter**, plus **Vanilla**. Decorative snow, pumpkins, lanterns, hearts, stars and other motifs move gently; animation off freezes them. Intensity and speed do not change item behavior.

The themes replace the survival inventory's panel and slot backgrounds while Minecraft still draws your actual skin, items, empty armor/offhand icons, tooltips and recipe controls. Recipe-book movement uses the screen's actual position. Item textures, slot positions, clicks, crafting and transfers keep their normal behavior. Creative and other container screens retain their existing appearance. Cosmetic preferences save separately in `config/economy-tracker/inventory-themes.json`, with atomic replacement. Corrupt files are retained and reported rather than silently overwritten.

All existing tracking/configuration paths and features remain compatible. The Minecraft **26.3 / Java 25** build still requires Fabric Loader **0.20.1+** and Fabric API for **26.3**.

| Minecraft | JAR | Java | Fabric Loader |
| --- | --- | --- | --- |
| **26.3** | [0.13.1 download](downloads/economy-tracker-0.13.1-mc26.3.jar) | **25** | **0.20.1+** |
| **1.21.11** | [0.11.0 download](downloads/economy-tracker-0.11.0-mc1.21.11.jar) | 21 | 0.18.4+ |

Install **Fabric API for the same Minecraft version**. The 26.3 build was compiled against Fabric API **0.166.2+26.3**. Replace the old Economy Tracker JAR; keep one build installed. Minecraft's official release name is **26.3**, not `1.26.3`.

The 26.3 adapter uses the actual Minecraft SDK with Mojang names, the new GUI extraction/screen APIs, RenderPearl texture pipelines, SDL keyboard/mouse codes and native asynchronous PNG chooser. It reads the new optional team-color scoreboard slot. Auction item stacks wait for joined-world component data and the picker cache resets between connections/worlds.

**Existing tracking/configuration data keeps its format and location.** The core, wallet, ledger, loan, auction and text-data classes are carried over byte-for-byte from 0.11.0. The compatibility build does not enable server commands or payment automation.

The dashboard replaces the initial O-key menu with an advanced **Christmas dashboard**, inspired by the reference layouts: dark cards, purple sidebar navigation, clear ON/OFF controls, colored lights, snowy trees and presents. Snowfall can animate gently or remain still.

Press **O** or `/economy` to open it. The sidebar provides:

- **Overview**: a purple welcome banner with the logged-in username and actual local player model/skin, followed by observed balance, profit since reset, money in/out, a smooth balance-history chart, recent bets and top winners/losers. Empty or unknown readings remain empty; charts do not invent a zero starting balance.
- **HUD modules**: explicit ON/OFF controls for Balance, Profit, Player Nets, Recent Bets, Rakeback and Loans.
- **Appearance**: independent panel sizes, RGB colors, opacity and designs with live preview.
- **Text boxes**: movable text boxes, individual line design, bubble font, inline pictures and a reference-style preset.
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
- Inter, Inter bold, Minecraft or **Bubble** font.
- **Start** and **End** font colors entered as six-digit RGB hex values (a leading `#` is accepted).
- **Grad: Solid / Across / Down**. Across blends character colors; Down renders clipped horizontal bands for a vertical gradient through the letters, including single-line text.
- A **Mint/Lime** reference preset (`49E8D4` → `55C900`) and Glass, plain Panel or off background.

**Done** or **Esc** saves; **Cancel** restores the text boxes from when the editor opened. Hidden boxes stay dimly visible for positioning in the editor but disappear from the in-game HUD. Text boxes remain visible independently of the rotating data tabs. Positions are saved relative to the viewport and remain within it when resolution changes. Up to 32 boxes, 1024 characters per box and 24 lines are supported.

### Individual lines, icons and bubble lettering

Open **O → Text boxes** and select a box. **Preset** creates the reference-style layout in that box: your current Minecraft username, large outlined bubble headings, minimum labels and rounded dark rows with colored dots, original game icons and optional side notes. Other boxes are unaffected. Text and numbers in this preset are editable examples, not enforced betting limits. **New** still creates a simple text box.

Click **Lines** to edit one line at a time. **Prev / Next** selects the line, and the text and optional side-note fields stay available on every tab:

- **Text**: per-line font, font size, start/end colors, Solid / Across / Down gradient, and outline. **Custom** applies those text settings only to this line; **Box style** restores inheritance from the main box's font/colors/gradient.
- **Row**: Plain, rounded Pill or rounded Card background, background color/opacity, optional colored dot, and side-note color. Pill/card rows share the box's measured width.
- **Image**: cycle the included icons with **Next image**, or click **Import PNG** to choose your own image from your computer. The selected image appears to the left of this line's text. **+ / -** controls image size; **Clear** removes the image. Aspect ratio and transparency are preserved.

The **Bubble** face uses the bundled, openly licensed **Lilita One** font, with optional dark outlines. [Font license](src/main/resources/assets/economy_tracker/font/bubble-font-license.txt). The eight built-in cards/roulette/dice/joker/gem/arrows/coin/heart icons are original artwork generated by `tools/create_text_icons.py`.

Images are copied into `config/economy-tracker/text-images/`; originals stay intact. PNGs may be up to **4 MB** and **2048 × 2048** pixels. Up to 128 imported files are supported. **Folder** opens the local image folder and **Refresh** reloads copied/manual images. Missing or invalid images show a small placeholder without interrupting the HUD. The mod makes no image URL/network requests. Imported image files are local resources; cancelling a style edit leaves the copied image available for later use.

The line editor's **Done** applies changes and returns to the text-box drag preview. **Cancel** restores that box to when the line editor opened. Use the parent text editor's **Done** to save everything, or its **Cancel** to restore all boxes, including nested line styles. Older simple text boxes retain their existing appearance. Adding/removing text lines preserves styles by line index and trims styles for deleted trailing lines. Text, image, note and row geometry is included in the same viewport fitting and drag bounds.

[Styled text layout preview](downloads/preview-0.11.0-text-lines.png) — synthetic sample text, not a live Minecraft screenshot.

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

Choose the JAR for your Minecraft version from the table above. Use matching Fabric API and the required Java/Fabric Loader versions. Keep one Economy Tracker JAR installed.

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

## Minecraft 1.21.11 build and verification

```sh
python3 tools/build.py
```

The build compiles retained sources against the checksum-pinned 0.10.1 release. Its existing native integration is retained. The world-hologram client tick and scope-cleanup adapters remain unchanged. The existing auction/payment guards, countdown and custom-parser bypass remain in place. A narrowly scoped Fabric access widener exposes only five display-entity setters for camera-facing text and label appearance. The dashboard, line/image editor, bubble font components, native GUI texture drawing, item picker, world item/text display and live player welcome banner compile against narrow API fixtures checked against Yarn 1.21.11 mappings. Fixtures and sample-data render harnesses remain excluded from the mod JAR.

Native-free checks execute location capture/persistence, stationary world entities, active-only glow, bob/rotation age independent of despawn, dimension/distance behavior, owned-ID cleanup, no auction HUD overlay, and cumulative bidding, exact deadlines, ties, empty results, overflow/validation, persistence, corruption and write failures, interruption, payment/bet isolation, native adapter hooks, item selection, winner rendering calls, the current-user player model, navigation and resizing. The existing financial ledger, wallet and loan model/store classes remain byte-for-byte unchanged. Payment capture adds auction receipt routing; ordinary payment and tax behavior retains regression coverage.

`DashboardPreview` in the fixture harness exports explicitly synthetic draw captures. `python3 tools/render_dashboard_preview.py` renders them with Pillow and the bundled Inter fonts for layout inspection.

Requires Java 21 (`JAVA_HOME`), JUnit Console Standalone (`JUNIT_JAR`), Gson (`GSON_JAR`), ASM 9.8 (`ASM_JAR`), ASM Tree 9.8 (`ASM_TREE_JAR`), Brigadier 1.3.10 (`BRIGADIER_JAR`) and JOML 1.10.8 (`JOML_JAR`). Defaults use the prepared workspace dependency cache.

**108 regression tests** cover winter preferences, settings transactions/checkpoints, independent HUD toggles, responsive geometry, bounded animation, balance interpolation without fabricated activity, text editing and persistence, Unicode measurement, drag bounds and viewport fitting, gradient endpoints, glass geometry and tinting, independent six-panel appearances, RGB adjustments, settings migration/persistence, validation, preview isolation, scale/geometry, fade colors, cancel/reset behavior, payment/tax behavior, balance reconciliation, bulk rakeback reset, balanced Player Nets, manual loans, repayments, No Loan list restrictions and repayment exceptions, schema migration, mixed-section pagination, persistence and server/account isolation, invalid input and file handling, failed saves, pagination, settings migration, and four-view rotation/fading. Eight adapter smoke suites exercise command scope guards, persistence, rendering calls, unsigned loan amounts, editor layout/toggling, save failures, world-hologram lifecycle/glow, and per-line text/image design, import, persistence, cancellation and rendering calls. These are headless checks; they do not connect to a live DonutSMP account.

The hologram's native method/field selectors, protected client glow flag, overridable render-age getter, and all five private access-widener targets were additionally verified against the SHA-checked official Minecraft **1.21.11** client JAR and Fabric intermediary mappings. Re-run with cached official inputs:

```sh
python3 tools/verify_hologram_api.py --client /path/to/client.jar --mappings /path/to/mappings.tiny --version-info /path/to/version.json
```

These checks do not replace live in-game rendering validation.

Per-line tests additionally cover legacy settings, independent styles, deep-copy cancellation, inheritance and gradients, line removal, small viewport fitting, PNG validation/import, transparent images, thumbnail bounds, file limits, missing files and symlink rejection. `StyledTextSmoke` checks the real UI paths against native-free API stand-ins. Imported textures use a bounded 128-entry cache of thumbnails no larger than 256 pixels, with disposal on refresh/eviction.

`tools/verify_text_api.py` verifies the new font/texture/folder API selectors against the SHA-checked official 1.21.11 client using the same arguments as the hologram verifier. The PNG chooser signature was also checked against Minecraft's bundled LWJGL TinyFD 3.3.3 library. `StyledTextPreview` and `tools/render_styled_text_preview.py` produce the explicitly synthetic layout preview using the shipped font and icons. Live Minecraft rendering still needs an in-game check.

## Minecraft 26.3 build

```sh
ECONOMY_JAVA25=/path/to/jdk-25 python3 tools/build_mc26.py
```

`tools/prepare_mc26.py` caches the hash-checked official 26.3 client SDK, Fabric Loader 0.20.1 and Fabric API 0.166.2+26.3. `ECONOMY_MC26_CACHE` selects the cache directory. The compiler-only SDK copy exposes exactly the five methods covered by the release's **official-namespace access widener**; the SDK/game classes are never bundled. The native adapter sources live under `src/mc26/java`. A client-only, official-name inventory mixin replaces just the survival panel draw and label color; Mixin libraries are compiler/runtime dependencies and are not bundled. Shared data/business classes and existing assets come from the checksum-pinned 0.11.0 JAR, while the native classes are built against the real SDK, without API stand-ins.

The build runs **116 regression tests on Java 25** and repeats them against the packaged deliverable. It rejects stale intermediary Minecraft references and bundled SDK/API fixtures. Fifty-one data/business classes, including the existing stores and financial logic, remain byte-for-byte identical to the 1.21.11 release.

`python3 tools/smoke_mc26.py` starts an isolated offline test profile with a separate **test-only** Fabric diagnostic mod, using SDL's offscreen OpenGL driver. It exercises native menu rendering, bubble text/images, PNG texture upload, SDL mouse dragging, saving/reloading, and a local flat-world auction. Synthetic data is labeled in captures; the diagnostic mod is excluded from release JARs. The profile never connects to DonutSMP or pays players. Runtime progress/results are written under `build/mc26/smoke-game/`; the log is `build/mc26/native-runtime.log`. Audio assets and online authentication services are outside that smoke's scope.

The 26.3 native smoke **passed**: 72 responsive native menu layouts (320×240, 480×360, 960×700 and 1280×720), with no overlapping or offscreen button/input bounds, plus inventory theme draft cancellation, save/load, native Christmas/Halloween/New Year inventory rendering, menu and text/image editing, native PNG texture upload, SDL mouse dragging, save/reload, live item registry, in-world player/skin rendering, and the auction hologram's location, active glow, winning bidder and removal. Testing used a disposable offline flat world; live DonutSMP payment capture has not been retested on 26.3.

Eight inventory regression tests cover default/round-trip preferences, immutable draft isolation, validation, corrupt/oversized files, failed saves, unrelated-file preservation, symlink rejection, distinct bounded artwork, animation freezing, speed, density levels, screen-wide distribution and rendering budgets. `src/mc26/test/java` is specific to the 26.3 build.
