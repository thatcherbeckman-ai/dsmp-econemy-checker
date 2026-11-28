# thatchacats econemy checker — Fabric 26.3 and 1.21.11

Version **0.23.0** refreshes the menu with **brighter colors, a mouse-following mini character, and menu transparency controls**. The default **Aurora** palette uses blue/lavender backgrounds, mint edges and colorful highlights. The little character appears at the screen’s bottom-right, beside the menu rather than over its buttons.

Open **O → Settings → Backgrounds → Display**:

- **Character**: turn the mini avatar on/off, enable/disable mouse following, and adjust its size from **60–140%**. It uses your own Minecraft skin, turns its head/body toward the pointer and fits into the available outside margin. Its render state is separate from your live player and the larger scenic character. It does not intercept clicks or change the camera. Dedicated full-screen scene/placement canvases keep their existing presentation.
- **Transparency**: adjust menu **Text** from **25–100%** and information **Panels** from **10–100%**. **Soft glass** sets text to 85% and panels to 35%; **Solid** restores full opacity. High contrast keeps these menu text/panels opaque. Text inputs remain readable. These are menu appearance controls; recorded values and in-game tracking behavior are unchanged.
- **Palettes**: choose **Aurora, Candy, Mint, Sky, Sunset or Classic**. The colors apply to menu cards, buttons, outlines, navigation and highlights. Original/Festive backgrounds use the chosen palette’s gradient; **Show palette background** selects that background with clear image effects. Existing custom images, solid RGB colors and seasonal scene choices remain available. **Classic** restores the burgundy styling.

All changes preview on the actual menu. **Done/Save** stores the appearance; **Cancel** restores the saved look. New preferences are kept in the existing `config/economy-tracker/menu-background.json` and included in cosmetic profiles/backups. Older preferences receive the new Aurora/mini-avatar defaults; saved scene, captions and other existing settings retain their formats. This update is cosmetic.

[Bright Aurora menu and mini character](downloads/preview-0.23.0-aurora-avatar.png) · [Candy palette](downloads/preview-0.23.0-candy.png) · [Transparency controls](downloads/preview-0.23.0-transparency.png) — native Minecraft captures from an isolated offline profile using its sample account’s default skin. Your client uses its own logged-in account/player skin through Minecraft’s skin manager.

Version **0.22.0** adds **O → Shaders**, an optional integration with an installed **Iris** mod. The Shaders section stays in the menu’s right-hand panel and reports the installed Iris version, enabled state and whether a shader pipeline is currently rendering.

- **Shader packs & options** opens **Iris’s own full-screen shader selector**, where you can choose an installed shader pack, apply it, adjust pack options and open Iris’s shaderpacks folder. Iris’s Done/Esc returns to the same Shaders page and restores the sidebar.
- **Shaders on / Shaders off** applies the change through Iris’s public configuration API. Iris handles saving and reloading; a shader reload may briefly pause rendering.
- **Refresh status** checks Iris again. Shader state is also refreshed when returning from Iris’s screen.
- If Iris is missing or its public API cannot be loaded, shader controls stay disabled and the rest of the mod remains usable. **Get Iris** opens the official Iris download site. Install Iris and its required Sodium version for **Minecraft 26.3**, then restart Minecraft. Iris and shader packs are not bundled or installed automatically.

Iris owns shader settings and its normal Apply/Done behavior. These changes are immediate: the economy menu’s Save/Cancel does not undo Iris changes. No shader configuration files are edited directly by this mod. The bridge uses only Iris’s supported `IrisApi.getInstance()`, `openMainIrisScreenObj(Object)`, `getConfig()`, `isShaderPackInUse()`, `areShadersEnabled()` and `setShadersEnabledAndApply(boolean)` methods, resolved only when Fabric reports Iris installed. API signatures and parent-screen behavior were checked against the official [Iris 26.3 API](https://github.com/IrisShaders/Iris/blob/671a5c87747d33983d7ed659461a64f46dfcb1f0/common/src/api/java/net/irisshaders/iris/api/v0/IrisApi.java), [configuration API](https://github.com/IrisShaders/Iris/blob/671a5c87747d33983d7ed659461a64f46dfcb1f0/common/src/api/java/net/irisshaders/iris/api/v0/IrisApiConfig.java), and its shader screen implementation. There is no required Iris dependency, copied Iris implementation, shader renderer or shader pack inside the release JAR.

[Shaders page without Iris](downloads/preview-0.22.0-shaders-missing.png) · [Iris controls / API contract test](downloads/preview-0.22.0-shaders-controls.png) — native Minecraft captures from an isolated offline test profile. The enabled example uses an explicitly labeled API test double. **Real Iris plus a real shader pack has not been run in this environment**; shader compilation/rendering and your installed Iris/Sodium combination still need an in-game check. Existing data services, Spotify playback code and historical releases are preserved.

Version **0.21.2** adds **smaller Spotify widgets and precise positioning**. Spotify size now ranges from **25% to 150%**, with five-percentage-point menu steps and exact whole-percent entry. Existing saved anchors, source and appearance remain compatible.

Open **O → Spotify → Overlay → Move / exact size** for a full-screen canvas using the same coordinates and bounds as the actual Spotify HUD:

- **Drag the box** anywhere, including up to a screen edge. Grabbing retains the pointer’s offset, and the box stays within the viewport.
- **Arrow keys** move one Minecraft GUI pixel; **Shift + arrows** move ten. The Left/Right/Up/Down buttons use the same steps.
- **Scroll on the box** to change size by five percentage points; hold **Shift** for one-point steps. Wheel resizing preserves the box’s center where screen bounds allow it.
- Type **X**, **Y** and **size %**, then choose **Apply** to preview the exact values. The fields keep pending edits when focus changes. Values are clamped to the size range and visible screen area.
- **Save** also applies any valid pending numeric fields, writes the current Spotify appearance/placement and returns to the same menu panel. **Cancel/Esc** discards the canvas draft. A later parent Cancel does not undo an explicitly saved canvas.

The toolbar moves to the opposite side of the box so it stays easy to drag. A sample song is shown when no music is available. The normal Overlay preview reflects smaller sizes. The nine existing position presets remain available; selecting and saving one returns to that anchor. Custom coordinates are stored relative to the available viewport area, so they adapt to Minecraft GUI/window resizing. Displayed pixel coordinates refer to Minecraft GUI pixels. Custom placement is preserved through palette/visibility changes, desktop-reader Start/Stop and source changes; menu resizing retains the current top-left pixel position.

[Precise Spotify placement](downloads/preview-0.21.2-spotify-placement.png) · [Smaller overlay controls](downloads/preview-0.21.2-spotify-overlay.png) — native Minecraft captures, isolated offline sample profile. This changes Spotify presentation only; Windows playback integration retains the 0.21.1 implementation and its documented on-device verification limits.

Version **0.21.1** adds **Spotify desktop mode for Windows**, so the overlay can read the Spotify desktop app’s local media information without a developer account, Client ID, Premium subscription or browser authorization. This mode does not call Spotify’s Web API. Spotify must be playing on the **same Windows PC** as Minecraft, and its song information must be available through Windows media controls.

To use it:

1. Replace the previous mod JAR with the **0.21.1 Fabric 26.3** download, then start Minecraft.
2. Open the **Spotify desktop app** and play a song.
3. In Minecraft press **O → Spotify → Desktop app → Use desktop app**. This immediately saves Desktop as the source and enables the overlay.
4. Choose **Overlay settings** to set position, size, colors and idle visibility, then **Done**.

Desktop mode reads the title, artist and playing/paused state through Windows’ documented **GlobalSystemMediaTransportControlsSessionManager** API. It accepts only Spotify desktop session IDs, including the Microsoft Store edition. Song progress is shown when the app supplies a usable timeline; otherwise the HUD says **Progress not supplied by app**. Windows does not supply a Spotify song link, and this mode does not control playback, record audio or retrieve account credentials. It requires **Windows 10 version 1809 or later, or Windows 11**, with the built-in **Windows PowerShell 5.1** and Windows media API available. Browser playback, macOS/Linux and playback on a phone or other PC are outside this local mode.

One hidden, noninteractive helper runs the script packaged inside the JAR. It loads no PowerShell profiles, downloads no code, performs no account/network requests, and does not change execution policy or require administrator rights. Metadata is read in memory, bounded and sanitized. A timeout kills an unresponsive helper and delays retries; stopping the reader, changing source, disabling the overlay or quitting Minecraft ends it. The helper also watches Minecraft’s process so it can exit after an abnormal shutdown. **Stop Spotify reader** immediately stops the reader and saves the disabled preference, including across Minecraft restarts. A saved enabled Desktop mode resumes on the next launch. Appearance controls retain Done/Cancel drafts; Start/Stop are immediate actions.

The existing account-linking method remains under **Spotify → Account (advanced)**, subject to Spotify’s developer access and Premium eligibility requirements. Existing 0.21.0 preferences keep Web API mode until Desktop is selected; new preferences recommend Desktop mode. Switching source clears prior playback and session credentials, and late responses cannot restore the previous source. Existing data services and historical JARs are preserved.

[Desktop mode menu](downloads/preview-0.21.1-spotify-desktop.png) · [Overlay controls](downloads/preview-0.21.1-spotify-overlay.png) — native Minecraft captures from an isolated offline profile. **Live Spotify on Windows is not verified here:** this build environment runs Linux. The packaged helper is syntax checked, the Microsoft WinRT method signatures are checked against Microsoft’s Windows bindings, and the source/process lifecycle is covered by simulated media/process tests; the actual Windows desktop session still needs an on-device check.

Version **0.21.0** adds **O → Spotify** in the existing right-hand menu panel. Link your Spotify account through Spotify’s browser sign-in, then show a rounded **Now playing** HUD with the song title, artist, playing/paused state, elapsed time and progress bar. Spotify supplies song details; audio continues playing in your Spotify app. The overlay works independently of the connected Minecraft server.

**Spotify setup (a registered Spotify developer app is required):**

1. Open **Spotify → Account → Developer dashboard** and create an app with **Web API** access, subject to Spotify’s current developer-account requirements.
2. Add this exact redirect URI to that app: `http://127.0.0.1:43821/callback`. The menu’s **Copy redirect URI** button copies it.
3. Copy the app’s public **Client ID** into the menu. Do not enter its Client Secret or your Spotify password. Your Spotify account must be permitted to use that developer app.
4. Press **Connect Spotify**, approve the read-only song permission on Spotify’s website, and return to Minecraft. Open Spotify on any device and play a song.
5. In **Overlay**, choose one of nine screen positions, **70–150%** size, six color palettes, and whether to hide the box while paused/idle. **Done** or the dashboard **Save** applies the draft; **Cancel** or switching sections discards unsaved appearance changes.

Connecting saves the Client ID and enables the overlay immediately. OAuth uses a random state, **PKCE S256** and a three-minute, one-use callback bound only to `127.0.0.1`. Only `user-read-currently-playing` is requested. Access/refresh tokens stay in memory and are cleared by **Disconnect** or closing Minecraft; **reconnect after restarting the game**. Public appearance preferences and the Client ID live in `config/economy-tracker/spotify.json`; credentials are never written there or added to appearance profiles. **App permissions** opens Spotify’s page for revoking access. This mod does not include a shared developer app or Client Secret.

Requests run off the game thread, poll at most every five seconds, honor rate-limit cooldowns, refresh expired tokens, reject unsafe song links and bound both request duration and response size. A busy callback port, cancelled sign-in, missing app access or unavailable Spotify is reported in the Account panel. Unsupported playback types such as podcasts/advertisements show no active song. Existing game data and the 1.21.11 release retain their previous formats and behavior.

[Spotify account setup](downloads/preview-0.21.0-spotify-account.png) · [Spotify overlay controls](downloads/preview-0.21.0-spotify-overlay.png) — native Minecraft captures from an isolated offline sample profile. **Real Spotify account authorization has not been tested:** Spotify domains are blocked in the build workspace. Protocol behavior is tested with local callbacks and fake Web API responses; use your own app and account to complete the real sign-in.

Version **0.20.0** adds a larger **Text** editor and a new **Style** editor under **O → Text boxes**. Select or create a box, then choose **Text** for real multiline editing: Enter inserts a new line, with the existing limits of 24 lines, 160 characters per logical line and 1,024 characters per box. Valid edits preview live; Cancel restores the previous content and styling.

**Style** stays in the right-hand panel and provides:

- **Fonts**: direct Inter, Inter bold, Minecraft and Bubble choices, plus bold, italic, size and alignment controls.
- **Colors**: 24 named color swatches over two pages, with custom six-digit RGB hex colors for the start, end, outline, shadow and background.
- **Gradient**: Solid, Across or Down, start/end color selection, color swapping and six ready-made gradient pairs.
- **Effects**: outline thickness and color, shadow color/offset, glass or rounded panel backgrounds, and background opacity.
- **Spacing**: letter spacing, line gaps and text opacity.
- **Presets**: 12 text styles—Neon Mint, Sunset, Ocean, Gold, Candy, Frost, Lava, Lavender, Emerald, Mono, Bubble Pop and Soft Rose.

Whole-box font/color controls also update existing custom row overrides, so the result appears immediately. Use **Lines** for individual row designs. Presets change styling while preserving the wording, box ID, placement, scale, inline pictures, notes and row shapes. The live preview can show the first three lines or the full box, and uses the same renderer/layout as the HUD. Vertical gradients respect the dashboard's scaled panel bounds.

The Style editor has up to 32 **Undo/Redo** steps. **Done** returns to the text-box editor; the parent editor's **Done/Save** writes the settings. **Cancel** restores the style/content draft. Existing text boxes load with compatible defaults, and style options are included in normal settings and appearance profiles. Financial models, wallet/history, recording controls and other economy data are unchanged. The 1.21.11 release is unchanged.

[Multiline editor](downloads/preview-0.20.0-multiline-text.png) · [Text-style presets](downloads/preview-0.20.0-text-presets.png) · [Expanded color palette](downloads/preview-0.20.0-text-colors.png) — native Minecraft captures, isolated offline sample profile.

Version **0.19.0** keeps the left sidebar in place when you open its sections. Appearance, text boxes, HUD layout, auctions, inventory themes and design tools now render in the **right-hand panel**. Nested settings and their Done/Cancel controls stay there too. Pointer events, text entry and dragging use the same panel transform as rendering, including small Minecraft GUI sizes. Explicit scene full previews and Player background → Start still fill the screen and return to the same dashboard/editor.

Each page keeps its own save rules. **Done or the dashboard Save button applies the active page**. **Cancel** discards that page's draft. Switching to a different sidebar section discards its unsaved page draft; this is shown in the dashboard footer. Saved loan edits and immediate profile/restore actions keep their existing behavior. Reclicking an active editor section does not discard it.

**O → Settings → Backgrounds** now previews and edits the actual new menu, using the same background renderer as the dashboard. Brightness, darkness, tint, blur, cards and particle controls affect that menu instead of a separate old backdrop. **Scenes → Festive menu** selects the current burgundy backdrop and Christmas lights.

The **Themes** tab adds the same 17 holiday/season choices as the inventory: Christmas, Halloween, New Year, Valentine’s Day, Lunar New Year, St. Patrick’s Day, Easter, Eid, Diwali, Hanukkah, Thanksgiving, Independence Day, Birthday, Spring, Summer, Autumn and Winter. Themes include their own palettes, small header motifs and falling/background decorations, with animation, speed and density controls. Reduced motion stops the animation. Menu theme choices save independently of the inventory theme. **Festive menu** clears the holiday choice.

The new **Colors** tab adds **Burgundy, Black, Charcoal, Navy, Teal, Forest, Purple, Rose and Sand** solid backgrounds. Select a preset, or enter a six-digit RGB hex color (for example `112233`) and press **Use hex color**. Changes preview on the surrounding menu immediately. **Done/Save** stores the selection; **Cancel** restores the saved background. Solid colors also save in appearance profiles. **Motion → Decor: Off** hides the lights when a plain look is preferred. Scenery and custom PNG choices remain available.

[Right-panel editor](downloads/preview-0.19.0-right-panel.png) · [Solid menu colors](downloads/preview-0.19.0-menu-colors.png) · [Holiday themes](downloads/preview-0.19.0-menu-holidays.png) · [Seasonal themes](downloads/preview-0.19.0-menu-seasons.png) — actual native Minecraft captures from an isolated offline sample account.

Version **0.18.0** changes the O-key menu into a **centered window** with the game visible around it. It uses the reference's dark burgundy/red palette, slim warm borders, small rounded rectangular controls, unboxed sidebar icons/text, Christmas light garlands and a bottom player/account card. Sidebar tabs have no button backgrounds or outlines; the selected tab uses a small red marker and keyboard focus uses a short underline. Their original click targets remain intact. Your own name, skin and existing sections remain in place. The window scales to the Minecraft GUI size and keeps a margin on every side.

Sidebar and settings-tab changes slide in with a short **180 ms ease-out** transition and a soft built-in **bubble pop**. The sound uses Minecraft's **UI** sound category and its volume settings. **Design tools → Access → Reduced motion** removes the sliding; it does not change your audio volume. Clicking the active tab does not replay the transition. Ordinary controls keep their native click feedback.

The main menu and its settings dialogs use the same window coordinates for rendering, clicks, scrolling, text selection and scene-preview dragging. Outside-window clicks are ignored, and changing tabs or reopening a parent dialog keeps the window size stable. Existing custom scenery still appears inside the window when selected. HUD layout, text-box placement, scene drag/full-preview and player-background presentation keep their full-size workspaces so their screen positions remain accurate.

This update changes presentation and local UI feedback. Existing wallet/history files, recording opt-outs, loans and other recorded data keep their existing formats and behavior. It adds no server commands, transfers or automation.

[Windowed menu with unboxed sidebar](downloads/preview-0.18.0-windowed-menu.png) — actual native Minecraft capture, isolated offline sample profile.

Version **0.17.2** adds balance history to **O → Overview**. Select **1D**, **7D**, **28D** or **365D** above the balance/received/paid/profit cards to see the balance change and balance graph for that rolling period. The range applies to balance history only; the other cards are labeled as recorded totals, not period-specific figures. The current balance remains visible separately.

Changes use actual saved readings, including the last known reading at or before the period start when available. **Partial history** means the change begins at the first available reading inside the period. With no readings or only one reading, no change is invented. Existing retained balance readings are imported; installing the update cannot recover history that was never recorded. The graph spaces readings by actual time, and the interface refreshes while open.

Balance history is passive and local: it sends no chat or balance commands. Each server/account has its own hashed file under `config/economy-tracker/wallet-history`. It retains minute snapshots for 48 hours and hourly snapshots for about a year, plus an opening anchor/first reading, with at most 12,000 readings. Recent changes flush at most every 30 seconds and on normal disconnect/shutdown. Appearance profiles/backups do not contain or replace this history. Unreadable history files are preserved; the overview reports the issue and falls back to the existing ledger readings.

**O → Tracking → Tracking controls** adds three independent off controls:

- **Turn off bet recording** stops new automatic, custom-parser and manual bet records. Wallet balance readings and payment adjustments keep working. Existing result history remains stored.
- **Hide profit** hides the profit view and the result-related profit/received/paid cards in the HUD and overview.
- **Hide player results** hides player nets, winner/loser lists, recent results and rakeback. Hidden views also leave the HUD rotation.

**Turn off all results** applies all three. **Save/Esc** commits these choices; **Cancel** discards the draft. These are recording/visibility opt-outs, stored separately in `tracking-controls.json`; appearance/profile restoration cannot undo them. Hiding a panel does not erase prior records or independently stop its underlying calculations. An unreadable controls file disables recording and hides result panels while preserving the original file.

[Balance-history preview](downloads/preview-0.17.2-balance-history.png) · [Tracking-controls preview](downloads/preview-0.17.2-tracking-controls.png) — isolated offline test account, synthetic sample readings.

Version **0.17.1** keeps complete menu labels visible. Buttons fit the full label without adding an ellipsis; longer labels can use two lines when that gives a larger, clearer text size. Menu headings, descriptions, captions, status messages and welcome text use their full strings with width-aware sizing. Editable text fields retain their normal scrolling behavior.

The dashboard sidebar buttons are left aligned at a four-pixel inset, narrower and capped at 18 pixels tall. The brand is also left aligned; the separate T badge is removed so the name and subtitle have room. The arrangement respects the existing 100%, 115% and 130% accessibility text options. These are cosmetic changes; saved preferences and recorded data keep their existing format.

[Full-label sidebar preview](downloads/preview-0.17.1-full-label-sidebar.png) — isolated offline test profile.

Version **0.17.0** adds the seven requested design tools. There is no new background hotkey.

Open **O → Design tools**:

- **Profiles**: name and save a complete appearance, then switch using a row's **Use** button. Profiles include scenery/character/captions, menu accent, HUD appearance/layout/text boxes, inventory theme and accessibility. Saving an existing name updates it (case insensitive); select a row to delete it. Up to 24 local profiles are kept. Applying a profile saves immediately and makes a backup of the previous look.
- **Backups**: the previous committed appearance is backed up automatically before settings, scene, inventory-theme and accessibility saves. Up to 12 recent, distinct backups are kept; rows show local dates/times. **Restore** applies the chosen look and backs up the current one first. **Backup now** creates an explicit snapshot. Profile/restore actions are immediate and are not undone by the tools screen's Cancel button.
- **Access**: menu text at 100%, 115% or 130%, stronger contrast and a global reduced-motion setting. Access changes preview in the tools screen; **Done** saves, **Cancel** discards them. Reduced motion freezes background pan/particles, inventory decorations, character idle motion, hover transitions and world-item display animation, and stops automatic HUD rotation/fades. Individual animation preferences stay saved and resume when reduced motion is turned off. HUD/custom-caption font sizes retain their own settings.
- **Search**: search design settings by multiple words, including fonts, colors, skin/angle, drag, shadows, blur, inventory themes, profiles and accessibility. Results open the relevant editor/tab.

Open **O → Player background → Edit scene**:

- **Undo/Redo** buttons keep up to 64 draft changes. They cover scenery, fonts/colors/captions, lighting, positions and drag gestures. New edits after Undo replace the redo branch. A drag is one undo step. **Cancel** still discards the draft; **Done/Esc** saves it.
- **Layout → Drag scene** opens a full-window editing canvas. Drag the character or the title/subtitle group, with alignment guides and optional snapping. The regular right-hand preview can also be dragged on roomy GUI sizes. **Back/Esc** returns to the unsaved editor draft. Caption presets remain available; selecting a preset restores its fixed alignment. Free positions and guide settings save with the scene and profiles.
- **Light** adds direction, strength and shadow softness controls, plus background blur, a shadow toggle and a lighting reset. Changes preview live. The lighting controls update the cosmetic render state and projected ground/shadows; the real player/camera remains unchanged.

The **Appearance** panel editor also has Undo/Redo for panel sizes, colors, opacity and design. Its Cancel button restores the appearance from when that editor opened.

Profiles and backups are stored locally in `config/economy-tracker/designs/{profiles,backups}`. They use an explicit cosmetic-field allowlist and never contain or restore tracking/message/server preferences, balances, bet records, loans or other recorded data. Appearance application validates the files first, stages all replacements and rolls back completed replacements if a write fails. Accessibility is stored separately in `config/economy-tracker/accessibility.json`. Invalid/linked/oversized files are preserved and reported. Local PNG references keep using their existing image folders; profiles do not upload or duplicate pictures.

[Design tools preview](downloads/preview-0.17.0-design-tools.png) · [Scene drag/lighting preview](downloads/preview-0.17.0-scene-editor.png) — real Minecraft captures from an isolated offline test account.

Version **0.16.2** reduces Cinematic foregrounds to a low, narrow ledge beneath the character. Tall side banks are removed. The scenery stays visible above and beside the footing, with soft edges rather than a floor covering the whole lower picture. Each scene gets matching ground: rocky shore for starry mountains, pale sand for beaches, snow for winter, moss/petals for blossoms, purple flower details for lavender, stone paving for the city and bamboo path, and amethyst tones for the nebula. Ground colors follow the illustrated scene when Scenery lighting is enabled.

All mod-menu buttons are also shorter and narrower, with a width limit on broad controls. Button labels use a slightly smaller type size; long labels keep their hover tooltips. Pointer bounds match the smaller visible buttons, and keyboard activation remains native. The surrounding layout spacing is retained.

The update applies to **Player background → Edit scene → Scenery → Style: Cinematic**. **Start** and **Esc** keep their existing behavior.

[Updated foreground and compact buttons](downloads/preview-0.16.2-player-background.png) — isolated offline test profile.

Version **0.16.1** adds **Player background** directly to the O-key menu sidebar. Open **O → Player background**, then **Start** to show your saved player scene across the full game window. This view has no buttons or menu panels. Press **Esc** to return to the same Player background section. **Edit scene** opens the existing scenery/character/text editor, and the section includes a live preview.

Starting or leaving the view does not save preferences, turn the character layer on behind other menus, move your real player or change the game camera. If a character scene has not been created yet, a temporary cherry-blossom scene is used. Saved scene choices, angles, captions and formatting are retained. The normal editor's Full preview/Save PNG controls remain available separately.

[Player background menu preview](downloads/preview-0.16.1-player-background.png) — isolated offline test profile.

Version **0.16.0** adds a **Cinematic** style that uses the rich illustrated scenery from your reference collection. The original 18 pictures retain their detailed skies, reflections, forests, mountains and lighting. Your actual Minecraft skin looks out over them from a continuous textured foreground ridge, with scenery-matched color, contact shadows and a feathered transition into the illustration.

Open **O → Settings → Backgrounds → Character scene → Scenery**, select **Style: Cinematic** and choose your scenery. It is the default style for the character composer. The style button cycles **Cinematic → Blocks → Photo**. **Done** saves; **Cancel** keeps your prior menu. **Cinematic** uses illustrated scenery with a blended foreground; **Blocks** offers a complete voxel landscape with matching materials, trees, atmosphere, distance softening and gentle daylight rays. **Photo** retains the separate block/deck platforms from 0.15.0. Original/Gradient/imported PNG backgrounds keep their prior form.

The eight views and fine turn/tilt controls remain under **Player**. Overhead **Name** labels scale with the character; toggle them under **Text**. Custom captions still work. Integrated ground follows the scenery; separate platform materials/base shapes are available in Photo style. **Snow overlook** applies the Minecraft reference preset directly: an angled rear view, snowy ridge, mountains, starry sky and name label.

The terrain and character footing remain aligned when you resize the preview or change character size/position. Display landscapes use the game's current block textures, loaded locally at runtime. They do not move your player, modify the joined world or switch your real camera. Cinematic compositions keep the picture still while the character and foreground remain aligned. **Full preview → Save PNG** hides the controls for a clean local capture.

[Illustrated cinematic preview](downloads/preview-0.16.0-cinematic-illustrated.png) · [Snow overlook preview](downloads/preview-0.16.0-snow-overlook.png) — actual rendering from an isolated offline test profile, not your account’s skin.

Version **0.15.0** added **Character scenes** under **O → Settings → Backgrounds → Character scene**. Your actual Minecraft skin stands in a third-person scene overlooking the scenery gallery, with editable captions and a full-screen preview. Character scenes are off until you create/save one; the prior menu styling and scenic backgrounds remain available.

[Character scene preview](downloads/preview-0.15.0-character-scene.png) — real Minecraft rendering from an isolated offline test account, not your account's skin.

- **Scenery**: use all 18 scenic backgrounds, Original/Gradient, or an imported PNG. Choose your existing background or import a new PNG directly.
- **Player**: eight rear/side/front/angled presets, fine turn and tilt controls, character size, cape and contact shadows. Ten textured platform materials include grass, stone, oak, cherry, snow, moss, sandstone, quartz, dark stone and bamboo, plus No Platform. Base styles are a ground-connected **Ledge**, **Block**, or **Wide deck**.
- **Text**: editable title/subtitle with up to four lines each, `{player}` for your session username, separate hex colors and color presets, Inter/Bubble/Minecraft fonts, size, shadows and six caption positions.
- **Layout**: horizontal position, ground height, platform size, left/center/right presets, composition reset and Scenery/Bright lighting. **In menus** applies the composition behind menus or leaves it as an editable scene.

**Full preview** hides the editing panels. **Save PNG** briefly hides its buttons and uses Minecraft's local screenshot folder; **Back** returns to editing. **Done** or **Esc** saves the complete background/character/text draft atomically; **Cancel** keeps the previous scene. Closing the child editor after saving also updates its parent background picker so the parent cannot later overwrite the saved scene with an older draft.

Platforms use the current game's block textures with perspective mapping, shaded faces, contact/cast shadows and scene-aware color grading. Ledge/deck bases extend to the bottom of the image, with grass edging and a subtle atmosphere layer to ground the character. Character rendering uses a separate standing render-state snapshot: it does not move/spawn your player, change the world, or switch the real camera. The account's loaded skin is used in-world; before joining, Minecraft's own skin lookup/default is used. Armor and held items are hidden in the cosmetic snapshot. Game texture assets are loaded at runtime and are not bundled into the mod.

The polished rounded menu controls and existing inventory/background features remain intact.

The existing **Settings → Backgrounds** section offers a cherry-blossom forest inspired by the reference and **18 original scenic pictures**. Choose blossom dawn/moonlight, lantern gardens, autumn rivers, winter cabins, aurora lakes, misty pines, enchanted forests, alpine sunrise, ocean sunset, tropical lagoons, desert dunes, lavender fields, starry mountains, rainy city, space nebula, floating islands or bamboo forest. Original Christmas, Simple Gradient and Your Imported PNG bring the picker to **22 background options**, including the voxel Snow overlook.

[Menu background preview](downloads/preview-0.14.1-polished-menus.png) — actual Minecraft rendering in the isolated offline test profile.

The picker has four tabs:

- **Scenes**: four pages of choices and a reset to the original background.
- **Look**: brightness (50–150%), darkness (0–80%), four blur levels, six color tints with adjustable strength, four vignette levels, Cover/Contain fitting and card opacity (45–100%).
- **Motion**: animation and gentle image pan toggles, Auto/None/Petals/Snow/Fireflies/Stars/Leaves/Rain/Confetti particles, three density levels, speed (25–200%), particle opacity and Auto/Off/Christmas decorations.
- **Custom**: local PNG import, reuse or clear the selection, refresh cached textures, fitting and zoom (100–150%).

The whole screen and the preview show draft changes live. **Done** or **Esc** saves; **Cancel** returns to the existing background. Selecting a scenic picture starts with a readable dark overlay, translucent cards and gentle pan. Other menus use the saved choice. The original Christmas controls still apply to Original mode; menu backgrounds and inventory themes have independent settings.

PNGs are copied locally and never uploaded. Imports allow **8 MB**, up to **4096 × 4096** and **12 million pixels**, with a limit of **24 images**. The source file stays intact. Imports use bounded, content-addressed filenames; cancelling keeps the imported image available but does not apply it. Preferences save atomically in `config/economy-tracker/menu-background.json`; images live in `config/economy-tracker/menu-backgrounds/`. Corrupt preferences are preserved and reported. Image processing and GPU uploads are cached with a six-texture limit; images are prepared at a maximum of 1024 pixels on their longest side.

The fuller holiday falling effects and Christmas menu decorations from 0.13.1 remain available.

The existing **Inventory themes** section offers: original holiday palettes, themed slot frames and animated decorations for your survival inventory. It retains the cleaner 0.12.1 menus and the name **thatchacats econemy checker**.

[Inventory themes preview](downloads/preview-0.13.1-inventory-themes.png) — native Minecraft capture from the isolated offline test profile.

Press **O → Inventory themes**, choose a style, and click **Done**. The picker has a live preview, three pages of choices, animation on/off, Low/Medium/High effects, and speed from 25% to 200%. **Speed** resets to 100%. **Cancel** discards the draft; **Done** or **Esc** saves it. Vanilla is selected by default and restores your normal inventory/resource-pack backdrop.

Choices include **Christmas, Halloween, New Year, Valentine's Day, Lunar New Year, St. Patrick's Day, Easter, Eid, Diwali, Hanukkah, Thanksgiving, Independence Day, Birthday**, and **Spring, Summer, Autumn and Winter**, plus **Vanilla**. Decorative snow, pumpkins, lanterns, hearts, stars and other motifs move gently; animation off freezes them. Intensity and speed do not change item behavior.

The themes replace the survival inventory's panel and slot backgrounds while Minecraft still draws your actual skin, items, empty armor/offhand icons, tooltips and recipe controls. Recipe-book movement uses the screen's actual position. Item textures, slot positions, clicks, crafting and transfers keep their normal behavior. Creative and other container screens retain their existing appearance. Cosmetic preferences save separately in `config/economy-tracker/inventory-themes.json`, with atomic replacement. Corrupt files are retained and reported rather than silently overwritten.

All existing tracking/configuration paths and features remain compatible. The Minecraft **26.3 / Java 25** build still requires Fabric Loader **0.20.1+** and Fabric API for **26.3**.

| Minecraft | JAR | Java | Fabric Loader |
| --- | --- | --- | --- |
| **26.3** | [0.23.0 download](downloads/economy-tracker-0.23.0-mc26.3.jar) | **25** | **0.20.1+** |
| **1.21.11** | [0.11.0 download](downloads/economy-tracker-0.11.0-mc1.21.11.jar) | 21 | 0.18.4+ |

Install **Fabric API for the same Minecraft version**. The 26.3 build was compiled against Fabric API **0.166.2+26.3**. Replace the old Economy Tracker JAR; keep one build installed. Minecraft's official release name is **26.3**, not `1.26.3`.

The 26.3 adapter uses the actual Minecraft SDK with Mojang names, the new GUI extraction/screen APIs, RenderPearl texture pipelines, SDL keyboard/mouse codes and native asynchronous PNG chooser. It reads the new optional team-color scoreboard slot. Auction item stacks wait for joined-world component data and the picker cache resets between connections/worlds.

**Existing tracking/configuration data keeps its format and location.** The core, wallet, ledger, loan, auction and text-data classes are carried over byte-for-byte from 0.11.0. The compatibility build does not enable server commands or payment automation.

The dashboard replaces the initial O-key menu with an advanced **Christmas dashboard**, inspired by the reference layouts: dark cards, purple sidebar navigation, clear ON/OFF controls, colored lights, snowy trees and presents. Snowfall can animate gently or remain still.

Press **O** or `/economy` to open it. The sidebar provides:

- **Overview**: a purple welcome banner with the logged-in username and actual local player model/skin, followed by observed balance, profit since reset, money in/out, a time-spaced balance-history chart with 1D/7D/28D/365D selectors, recent bets and top winners/losers. Empty or unknown readings remain empty; charts do not invent a zero starting balance.
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

The build runs **156 regression tests on Java 25** and repeats them against the packaged deliverable. It rejects stale intermediary Minecraft references and bundled SDK/API fixtures. Fifty-one data/business classes, including the existing stores and financial logic, remain byte-for-byte identical to the 1.21.11 release.

`python3 tools/smoke_mc26.py` starts an isolated offline test profile with a separate **test-only** Fabric diagnostic mod, using SDL's offscreen OpenGL driver. It exercises native menu rendering, bubble text/images, PNG texture upload, SDL mouse dragging, saving/reloading, and a local flat-world auction. Synthetic data is labeled in captures; the diagnostic mod is excluded from release JARs. The profile never connects to DonutSMP or pays players. Runtime progress/results are written under `build/mc26/smoke-game/`; the log is `build/mc26/native-runtime.log`. Audio assets and online authentication services are outside that smoke's scope.

The 26.3 native smoke **passed**: 132 responsive native menu layouts (320×240, 480×360, 960×700 and 1280×720), with no overlapping or offscreen button/input bounds, plus the menu-background picker, cancellation/save/load, custom PNG and GPU uploads for all 18 scenic tiles, Original, Gradient and Custom, plus inventory theme draft cancellation, save/load, native Christmas/Halloween/New Year inventory rendering, menu and text/image editing, native PNG texture upload, SDL mouse dragging, save/reload, live item registry, in-world player/skin rendering, and the auction hologram's location, active glow, winning bidder and removal. Testing used a disposable offline flat world; live DonutSMP payment capture has not been retested on 26.3.

Eight inventory regression tests cover default/round-trip preferences, immutable draft isolation, validation, corrupt/oversized files, failed saves, unrelated-file preservation, symlink rejection, distinct bounded artwork, animation freezing, speed, density levels, screen-wide distribution and rendering budgets. `src/mc26/test/java` is specific to the 26.3 build.

Nine menu-background tests cover preferences/defaults, deep-copy draft isolation, invalid settings, corrupt/oversized files, atomic/save failure behavior, PNG import preservation, safe filenames and symlinks, size/dimension limits, image blur/brightness/tint and resizing, bounded/frozen particle geometry, density and packaged scenic atlas geometry. The native smoke also exercises all four picker tabs at four screen sizes and cycles through every background on the real texture manager.

The 0.14.1 native smoke additionally checks that all four antialiased surface masks upload, the restyled input fields accept typing/backspace, and native keyboard activation still works. Its 100 layout cases cover the same compact and large viewports. HUD/data fonts, financial logic and existing stored data retain their prior behavior.

Nine character-scene regression tests cover migration from older preferences, atomic combined save/load, deep draft copying, caption validation/Unicode, camera/color limits, all platform placement extremes, nested-file corruption preservation, perspective texture detail/alpha/ground connection, and independent eight-view/lighting options. Native checks exercise the editor, text formatting, cancellation, persistence, all eight angles and platform materials, real player pose preservation, texture uploads and clean PNG capture.

The cinematic terrain checks cover all 18 earlier scenery palettes, safe image bounds, independent draft copies, reference-preset persistence, projected footing and transparent/feathered foreground geometry for all six pictures in the latest reference. The native diagnostic cycles all 18 illustrated and block sceneries through the real texture manager and verifies cinematic/name-label save/load and clean PNG capture alongside the existing 132 layout checks. Game block textures are loaded from the active resource manager, never distributed in the mod.

The 0.16.1 native check covers 140 responsive layouts including the new sidebar section and button-free presentation at four GUI sizes. It verifies Edit scene/Cancel return, Start, Enter staying in presentation, Esc returning to the same menu, repeated Start/Esc, unchanged saved scene bytes and unchanged real-player pose. A model test covers independent temporary scene defaults and preserving existing captions/views.

The 0.16.2 foreground check covers all 18 scenes at two sizes: no pixels above the small footing, clear side edges, under 14% foreground coverage in the tested compositions, solid support at the feet and scenery lighting colors. Native checks retain 140 responsive layouts and additionally check that compact button hitboxes exclude their former corners, accept center clicks and still respond to the keyboard.

The 0.17.0 design checks cover profile allowlisting/independent copies and name overwrite, file/name/count limits, bounded/deduplicated backup retention across restarts, corrupt/symlink preservation, staged multi-file save failure, accessibility validation, undo/redo branches and limits, drag clamping/snapping, lighting serialization/shadows and multiword search. Native verification includes 164 responsive layouts, profile save/use/delete, actual backup restoration, accessibility preview/cancel/save and reduced motion, search opening the Light tab, character/caption drag with one-gesture Undo/Redo, panel appearance Undo/Redo and lighting/blur persistence. A ledger snapshot is checked before and after profile/restore actions.

The 0.17.1 label checks cover unbroken names, Unicode, whole-word wrapping and compact dimensions across accessibility scales. The native suite runs 492 menu layout cases (four GUI sizes at three text scales), checks full button-label preservation and text bounds, verifies the compact four-pixel sidebar alignment, and retains profile, backup, drag, lighting, accessibility and Start/Esc checks.

The 0.17.2 checks cover the four rolling balance ranges, exact/partial/missing opening coverage, timestamp order and duplicate readings, overflow, year retention/compaction, local account/server isolation, atomic round trips and corrupt/symlink preservation. Recording opt-out checks cover separate persistence, unchanged prior results, wallet/payment/tax updates while paused and appearance-profile isolation. All **172 regression tests** pass against both compiled sources and the packaged JAR. Native verification includes **552 responsive layouts**, all four balance-range selections and history persistence, plus opt-out Save/Cancel/persistence, custom-result blocking, custom balance readings, and hidden overview/HUD views. The native run uses an isolated offline test account; live DonutSMP capture has not been retested.

The 0.18.0 checks add centered-window bounds and coordinate round trips across small/large viewports, monotone slide completion/restarts/reduced motion, and transformed native player-preview bounds. All **179 regression tests** pass against sources and the packaged JAR. Native checks cover **552 menu layouts**, real inverse-scaled navigation and moving-body control clicks, outside-click rejection, stable tab/dialog rebuilds, the UI sound category, all six rounded-surface masks, player previews, existing scene/HUD workflows and balance-history/recording-off behavior. Native audio output is silent in the isolated smoke profile; the pop uses Minecraft’s built-in UI sound asset.

The 0.19.0 unit suite passes **190 tests**, including right-panel coordinate transforms, exact solid-color pixels, real menu image effects, RGB validation/migration/copy/persistence, independent menu holiday choices, bounded/frozen/distinct motifs and palette restoration. The focused native check passes **145 panel cases** across all 12 sidebar sections, four sizes and three text scales, plus nested return, pointer/text entry, draft cancellation, solid/hex live preview/save/reload, holiday preview/animation/save/cancel, inventory independence, presentation return and unchanged recorded data. The broader native regression suite includes **576 responsive layouts** and the existing editor, profile, scene and history/recording-off checks. These are isolated offline checks; they do not connect to a live DonutSMP account. Financial core, wallet/history, loan, auction and recording-opt-out model classes remain byte-for-byte unchanged.

The 0.20.0 suite passes **199 tests** against both compiled sources and the packaged JAR. New checks cover legacy defaults, complete style copy/restore and settings round trips, preset content/position/image preservation, atomic invalid edits, bounded Undo/Redo, Unicode-aware letter spacing, scaled effect padding, measured drag bounds, override propagation and validation. A focused native check passes **72 style layouts** at four sizes and three text scales, plus real multiline Enter/typing, both swatch pages and custom hex input, preset Undo/Redo, vertical-gradient clipping in the scaled panel, effects/spacing/bold/italic, persistence and both style/content cancellation. The broader native regression suite passes **576 layouts** and retains editor/profile/scene/history checks. Diagnostic profiles stay offline and are excluded from release JARs.

The 0.21.0 suite passes **214 tests** against sources and the final packaged JAR. Spotify checks include the RFC 7636 PKCE vector, callback host/state/duplicate validation, cancellation, local end-to-end fake authorization, refresh/401/rate limits, bounded responses, safe metadata links, in-flight disconnect isolation, and preferences without credentials. Native verification passes **24 Spotify panel layouts** (four sizes, three text scales), pointer/text routing, invalid-ID rejection, appearance save/reload and cancellation, and sample playback rendering. The wider right-panel suite passes **157 layouts across all 13 sections** with existing nested navigation/theme behavior. Tests use an isolated offline profile and synthetic playback; real Spotify account sign-in is untested because the workspace cannot reach Spotify.

The 0.21.1 suite passes **226 tests** against compiled sources and the packaged JAR. Desktop checks cover legacy/new source preferences, local metadata validation/sanitization/Unicode, both Spotify source IDs, idle/paused/unknown timing, bounded output, the packaged encoded helper, unsupported platforms, streamed updates, single-helper ownership, watchdog/cooldowns, Stop and restart persistence, in-flight source isolation, and zero account API requests in Desktop mode. Windows helper syntax was checked with the checksum-verified official PowerShell 7.7.0 parser; live execution on Windows PowerShell 5.1 remains unverified. Native checks pass 37 layout/action cases covering the three Spotify pages at four sizes and three text scales, plus immediate Desktop Start/Stop, preferences, Cancel behavior and the actual HUD renderer. Real Windows media playback remains untested on this Linux profile.

The 0.21.2 suite passes **233 tests** against sources and the packaged JAR. Placement checks cover migration/round trips, all nine anchors and custom extremes across viewport sizes and 25–150% scales, exact pixel movement/clamps, center-preserving resizing, resolution adaptation, immutable cosmetic/source changes and Start/Stop preservation. Native checks pass **49 layout/action cases**, exercising all Spotify tabs and the full-screen canvas, exact fields across render/focus, actual pointer drag and edge clamps, wheel resizing, arrow/Shift movement, minimum size, pending-field Save, file reload, Save/Cancel/Esc returns and source/size placement preservation. Checks use offline sample content.

The 0.22.0 suite passes **240 tests** against compiled sources and the packaged JAR. Iris contract checks cover zero optional class loading when absent, unavailable/broken API handling, configuration versus actual rendering state, public interface calls on private implementations, explicit on/off and same-state behavior, parent identity, factory failures and bounded safe version labels. The focused native check passes **25 layout/return cases** across four sizes and three text scales, including missing-Iris disabling, render-thread toggle calls, full-screen selector, repeated Done/Esc restoration to the same page, immediate-change Cancel semantics and unchanged recorded data. The broader native suite also passes **169 layouts across all 14 sidebar sections**, retaining nested navigation, themes, typing/cancellation, presentation return and data preservation. The native selector is a test double; no actual Iris shader rendering is claimed.

The 0.23.0 suite passes **247 tests** against sources and the packaged JAR. New appearance tests cover legacy/default preferences, independent copies and round trips, validation, monotone alpha/RGB preservation, avatar screen/menu bounds, bounded pointer angles and fixed-facing mode, distinct brighter palette pixels and exact solid-color preservation. The focused native suite passes **36 display layouts** at four sizes and three text scales, real own-skin PIP extraction, independent avatar snapshots/pose/lighting, live toggles/size, palette and transparency preview, Save/reload/Cancel, six palette texture uploads, high-contrast display and inventory/data independence. The broader native suite also passes **169 layouts across all 14 sidebar sections**, retaining navigation, nested editors, scene previews and existing input/save behavior. Checks use a disposable offline profile.
