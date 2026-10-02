---
name: "叁序 · SanXu"
description: "原生 Android 数据工具的酒红、石墨与暖灰设计系统"
colors:
  light-primary: "#AD243B"
  light-on-primary: "#FFFFFF"
  light-primary-container: "#F4E3E6"
  light-on-primary-container: "#762035"
  light-secondary: "#61565A"
  light-on-secondary: "#FFFFFF"
  light-secondary-container: "#97243A"
  light-on-secondary-container: "#FFFFFF"
  light-tertiary: "#805221"
  light-background: "#F1EFEC"
  light-on-background: "#292427"
  light-surface: "#FFFEFD"
  light-on-surface: "#292427"
  light-surface-variant: "#ECE7E5"
  light-on-surface-variant: "#696063"
  light-outline: "#96898D"
  light-outline-variant: "#DDD4D5"
  light-surface-container: "#ECE7E5"
  light-surface-container-low: "#F8F5F3"
  light-surface-container-high: "#E6DDDE"
  light-error: "#AA2530"
  dark-primary: "#FFACBA"
  dark-on-primary: "#650C25"
  dark-primary-container: "#472630"
  dark-on-primary-container: "#FFD6DE"
  dark-secondary: "#D0C0C5"
  dark-on-secondary: "#382B30"
  dark-secondary-container: "#6F2334"
  dark-on-secondary-container: "#FFE8ED"
  dark-tertiary: "#DFC099"
  dark-background: "#171416"
  dark-on-background: "#F1E8EA"
  dark-surface: "#231E21"
  dark-on-surface: "#F1E8EA"
  dark-surface-variant: "#342B30"
  dark-on-surface-variant: "#CBBCC2"
  dark-outline: "#9C8990"
  dark-outline-variant: "#4B3C43"
  dark-surface-container: "#30272C"
  dark-surface-container-low: "#1D181B"
  dark-surface-container-high: "#3C3037"
  dark-error: "#FFB4AD"
typography:
  headline-large:
    fontFamily: "system-ui"
    fontSize: "28sp"
    fontWeight: 600
  headline-medium:
    fontFamily: "system-ui"
    fontSize: "20sp"
    fontWeight: 600
    lineHeight: "26sp"
  title-large:
    fontFamily: "system-ui"
    fontSize: "20sp"
    fontWeight: 600
  title-medium:
    fontFamily: "system-ui"
    fontSize: "16sp"
    fontWeight: 600
  body-large:
    fontFamily: "system-ui"
    fontSize: "16sp"
    lineHeight: "24sp"
  body-medium:
    fontFamily: "system-ui"
    fontSize: "14sp"
    lineHeight: "21sp"
  body-small:
    fontFamily: "system-ui"
    fontSize: "12sp"
    lineHeight: "18sp"
  label-large:
    fontFamily: "system-ui"
    fontSize: "14sp"
    fontWeight: 500
  draw-digit:
    fontFamily: "monospace"
    fontSize: "30sp"
    fontWeight: 700
  digit-key:
    fontFamily: "monospace"
    fontSize: "20sp"
    fontWeight: 500
  digit-key-selected:
    fontFamily: "monospace"
    fontSize: "20sp"
    fontWeight: 700
  choice-active:
    fontFamily: "system-ui"
    fontSize: "14sp"
    fontWeight: 600
  context-tag:
    fontFamily: "system-ui"
    fontSize: "12sp"
    fontWeight: 500
  navigation-label:
    fontFamily: "system-ui"
    fontSize: "12sp"
    fontWeight: 400
rounded:
  small: "8dp"
  medium: "12dp"
  large: "20dp"
  tag: "6dp"
  compact-action: "10dp"
  circle: "50%"
spacing:
  micro: "4dp"
  gap: "8dp"
  page: "12dp"
  inset: "16dp"
  empty-panel: "24dp"
components:
  page-header-light:
    backgroundColor: "{colors.light-secondary-container}"
    textColor: "{colors.light-on-secondary-container}"
    typography: "{typography.headline-medium}"
    padding: "0dp 8dp 0dp 16dp"
    height: "48dp"
  button-start-pick-light:
    backgroundColor: "{colors.light-primary}"
    textColor: "{colors.light-on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "0dp 12dp"
    height: "48dp"
  button-save-light:
    backgroundColor: "{colors.light-primary}"
    textColor: "{colors.light-on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.compact-action}"
    padding: "0dp 8dp"
    height: "48dp"
    width: "98dp"
  position-next-light:
    textColor: "{colors.light-primary}"
    typography: "{typography.label-large}"
    padding: "0dp 8dp"
  history-search-light:
    textColor: "{colors.light-on-surface}"
    rounded: "{rounded.medium}"
  navigation-light:
    backgroundColor: "{colors.light-surface}"
    textColor: "{colors.light-on-surface-variant}"
    typography: "{typography.navigation-label}"
    height: "64dp"
  choice-active-light:
    backgroundColor: "{colors.light-surface}"
    textColor: "{colors.light-primary}"
    typography: "{typography.choice-active}"
    padding: "0dp 4dp"
    height: "48dp"
  context-tag-light:
    backgroundColor: "{colors.light-primary-container}"
    textColor: "{colors.light-on-primary-container}"
    typography: "{typography.context-tag}"
    rounded: "{rounded.tag}"
    padding: "4dp 8dp"
  workspace-panel-light:
    backgroundColor: "{colors.light-surface}"
    rounded: "{rounded.medium}"
  draw-digit-light:
    backgroundColor: "{colors.light-on-secondary-container}"
    textColor: "{colors.light-secondary-container}"
    typography: "{typography.draw-digit}"
    rounded: "{rounded.circle}"
    size: "48dp"
  digit-idle-light:
    backgroundColor: "{colors.light-surface}"
    textColor: "{colors.light-on-surface}"
    typography: "{typography.digit-key}"
    rounded: "{rounded.circle}"
    size: "48dp"
  digit-selected-light:
    backgroundColor: "{colors.light-primary}"
    textColor: "{colors.light-on-primary}"
    typography: "{typography.digit-key-selected}"
    rounded: "{rounded.circle}"
    size: "48dp"
  position-active-light:
    backgroundColor: "{colors.light-primary-container}"
    rounded: "{rounded.small}"
  page-header-dark:
    backgroundColor: "{colors.dark-secondary-container}"
    textColor: "{colors.dark-on-secondary-container}"
    typography: "{typography.headline-medium}"
    padding: "0dp 8dp 0dp 16dp"
    height: "48dp"
  button-start-pick-dark:
    backgroundColor: "{colors.dark-primary}"
    textColor: "{colors.dark-on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.small}"
    padding: "0dp 12dp"
    height: "48dp"
  button-save-dark:
    backgroundColor: "{colors.dark-primary}"
    textColor: "{colors.dark-on-primary}"
    typography: "{typography.label-large}"
    rounded: "{rounded.compact-action}"
    padding: "0dp 8dp"
    height: "48dp"
    width: "98dp"
  position-next-dark:
    textColor: "{colors.dark-primary}"
    typography: "{typography.label-large}"
    padding: "0dp 8dp"
  history-search-dark:
    textColor: "{colors.dark-on-surface}"
    rounded: "{rounded.medium}"
  navigation-dark:
    backgroundColor: "{colors.dark-surface}"
    textColor: "{colors.dark-on-surface-variant}"
    typography: "{typography.navigation-label}"
    height: "64dp"
  choice-active-dark:
    backgroundColor: "{colors.dark-surface}"
    textColor: "{colors.dark-primary}"
    typography: "{typography.choice-active}"
    padding: "0dp 4dp"
    height: "48dp"
  context-tag-dark:
    backgroundColor: "{colors.dark-primary-container}"
    textColor: "{colors.dark-on-primary-container}"
    typography: "{typography.context-tag}"
    rounded: "{rounded.tag}"
    padding: "4dp 8dp"
  workspace-panel-dark:
    backgroundColor: "{colors.dark-surface}"
    rounded: "{rounded.medium}"
  draw-digit-dark:
    backgroundColor: "{colors.dark-on-secondary-container}"
    textColor: "{colors.dark-secondary-container}"
    typography: "{typography.draw-digit}"
    rounded: "{rounded.circle}"
    size: "48dp"
  digit-idle-dark:
    backgroundColor: "{colors.dark-surface}"
    textColor: "{colors.dark-on-surface}"
    typography: "{typography.digit-key}"
    rounded: "{rounded.circle}"
    size: "48dp"
  digit-selected-dark:
    backgroundColor: "{colors.dark-primary}"
    textColor: "{colors.dark-on-primary}"
    typography: "{typography.digit-key-selected}"
    rounded: "{rounded.circle}"
    size: "48dp"
  position-active-dark:
    backgroundColor: "{colors.dark-primary-container}"
    rounded: "{rounded.small}"
---

# Design System: 叁序 · SanXu

## Overview

**Creative North Star: "酒红与紧凑步骤"**

叁序是日常使用的原生 Android 数据工具。酒红身份区承担页面识别，石墨文字承担阅读，暖灰底与浅色内容面承托数字。视觉重心来自开奖号、正在编辑的位置和当前动作，减少大块留白，让用户在手机上完成一件清楚的事。

用户已经选择 B 纵向位选与开奖号优先的首页，随后明确要求颜色更有力度、组件更小、单屏信息更多。本文件提取修订后的 Compose 实现；这些选择不等于最终审美已获认可。记录时原生截图 QA 仍在进行，本文不宣布截图、业务流程或最终发布验收完成。

**Key Characteristics:**

- 酒红身份区、暖灰背景、石墨正文；浅色与深色分别定义原生角色。
- 紧凑内容分组，操作至少 48dp；内容区与系统安全区分别计算。
- 号码使用系统等宽字体，数字、文字、勾选与线条共同表达状态。
- 平台组件承担输入、反馈和模态行为，普通内容面不添加自定义阴影。

本文件是代码扫描后的设计系统记录，不重新发起视觉方向选择。产品事实见 [PRODUCT.md](PRODUCT.md)；页面构图与待验证的验收目标见 [设计规格](docs/design-spec.md)、[方向约定](docs/ui-research/direction-contract.md) 和 [原生区域规格](docs/ui-research/native-regions.json)。范围为 SanXu；原 Lucky3D 仍为只读参考。

## Colors

调色板是有力度的酒红、偏暖石墨与暖灰。前置令牌保留 `ColorScheme` 的角色关系，`light-` 与 `dark-` 仅标记主题；完整对应关系位于 sidecar 的 `extensions.native.themeRoleMap`。

### Primary

- `primary / onPrimary`：主要动作、当前数字、被选中的数字键、导航和页签的选中态。
- `primaryContainer / onPrimaryContainer`：当前编辑步骤、方案摘要和小型上下文标签；与身份区使用不同的容器角色。
- 浅色使用酒红主色，深色使用浅玫瑰主色保证状态在深背景上可读。两个主题均采用代码中的固定配色，不启用壁纸动态配色。

### Secondary

- `secondaryContainer / onSecondaryContainer`：酒红页头、状态栏背景和首页开奖区域。这是项目明确指定的身份角色，不改名为 primary 容器。
- `secondary / onSecondary`：保留已定义的辅助中性色角色；标准 Material 3 组件按原生语义读取。

### Tertiary

- `tertiary` 是代码中保留的暖棕角色，目前核心工作台没有直接使用它；不据此新增第三种主要强调色。

### Neutral

- `background / onBackground`：整页暖灰或暖黑背景与正文。
- `surface / onSurface`：内容面、输入周边与正文。
- `surfaceVariant / onSurfaceVariant`：变体色面与次要说明、未选中导航。
- `surfaceContainerLow / surfaceContainer / surfaceContainerHigh`：表头、输入上下文、未完成步骤标记等层次。
- `outline / outlineVariant`：轮廓、底部弹层拖拽柄、列表分隔与数字键边界。
- `error`：明确的校验或操作问题，不代替错误说明。未在 `SanxuTheme.kt` 显式覆盖的其他 Material 3 颜色继续由依赖库提供，本文件没有假造其值。

**The Native Role Pair Rule.** 按同一主题成对使用 ColorScheme 容器与其 on 色；保留原生角色名称，不用单一酒红常量替换所有语义。

## Typography

界面使用 Android 系统默认字体；号码通过 `NumberFont = FontFamily.Monospace` 使用系统等宽字体。前置 `system-ui` 与 `monospace` 是跨工具的字体类别描述，未安装或绑定网页字体；Android 的实际字体回退由系统决定。所有字号保留原生 `sp` 单位。

- 主题层保留 `headlineLarge`、`headlineMedium`、`titleLarge`、`titleMedium`、`bodyLarge`、`bodyMedium`、`bodySmall`、`labelLarge` 的实际声明。未显式指定的行高、字距、字重不在文档中补成新令牌。
- 页头使用 `headline-medium`，二级页和弹层标题使用 `title-large`，区域名使用 `title-medium`，正文与说明使用 `body-medium / body-small`。
- 首页三颗开奖号使用 `draw-digit`；数字键使用 `digit-key`，选中时使用 `digit-key-selected`。金额和其他长数字的缩字号仍按各组件现有代码处理。
- 日期与位置等极次要局部标签存在 11sp 例外，不把它扩大成普遍正文尺度。首页历史表的期号为 13sp、开奖号为 18sp，这些是页面局部值。

**The Data Has Shape Rule.** 号码保持三位与前导零；等宽数字、标签、勾选或线条提供可见状态，颜色不独自承担含义。

## Layout

全局按手机单列组织。原生验收目标是 360×800dp（API26，三键导航）与 393×851dp（API37，手势导航）；这是两个设备视口，不是 CSS 断点。主内容横向间距取 `spacing.page`，常规分组间距取 `spacing.gap`，内部细分取 `spacing.micro`。二级页面与弹层可使用 `spacing.inset`，不要把每个既有局部值强行统一。

页头内容区 48dp，状态栏安全区另外计算；底部五入口内容区 64dp，再叠加系统导航安全区。图标通常小于触控区，不能用图标边界代替操作区域。下列页面签名只约束相应页面，不是所有屏幕的通用模板：

| 场景 | 当前实现 |
|---|---|
| 首页 | 开奖区在先；48dp 开奖数字圆底；56dp 试机号行；合并为一个 48dp 表头的最近开奖表，列出当前主开奖之外的五期，每行 48dp；方案与开始选号同处至少 64dp 的行动行。 |
| 位选 | 四种方法页签、期号行和三条步骤标题均为 48dp；只展开当前位的两行五列十键区，数字键 48dp、行间 8dp，数字区总高 104dp；已选摘要始终保留。 |
| 位选结果 | 当前位工具行 48dp；预览最多四个号码；完整内容进入明细。金额、倍数和加入或更新动作留在独立底部操作区；满池结果不将全部号码铺进页面。 |
| 内容面与二级页 | `WorkspacePanel` 只提供面与形状，不自带内边距；`PaperPanel` 才自带 12dp 内边距。搜索、档案列表、工具与弹层按实际内容分组。 |

“首页第五行与开始选号同屏、位选明细与保存同屏”是本轮的原生验收目标，记录时尚未在本文中宣布完成。正常内容之外，错误和长内容允许使用实际页面的滚动与恢复路径。

**The Compact Content, Full Target Rule.** 通过减少留白、合并表头和只展开当前输入来提高密度；可操作区域不得因此小于 48dp。

## Elevation & Depth

普通 `Surface`、`WorkspacePanel`、`PaperPanel`、身份页头及表头未声明自定义 `shadowElevation` 或 `tonalElevation`；层次来自背景与内容面的对比、编辑浅底和细分隔线。应用未定义自己的阴影或动画时间曲线。底部弹层、Snackbar、Ripple、输入焦点等行为沿用 Material 3；不要把“普通内容平面化”误读为移除原生浮层或反馈。

**The Flat Content Rule.** 普通内容面靠色面、间距和分隔形成层级；模态弹层、输入反馈和按压反馈遵从原生 Material 3 行为，不新增装饰性阴影。

## Shapes

普通内容面使用 `rounded.medium`，步骤组和首页主动作使用 `rounded.small`，保存按钮与倍数容器使用 `rounded.compact-action`，上下文标签使用 `rounded.tag`。主题另保留 `rounded.large`，不因此扩大全部面板圆角。

数字键与开奖号均为 `CircleShape`；前置的 `rounded.circle = 50%` 仅是该形状的便携表示，对应正方形容器中的圆形。数字键未选中时为 0.5dp 轮廓，选中时为 1dp 主色轮廓，同时显示粗体和勾选。页签指示线为 28×3dp、圆角 2dp；底部导航指示线为 18×2dp、圆角 1dp。

## Components

### Buttons

主要按钮使用 `primary / onPrimary`，文本动作依附相关内容。首页“开始选号”采用 8dp 圆角、48dp 高度和横向 12dp 内容间距；位选“加入本期 / 更新记录”采用 10dp 圆角、98dp 宽、48dp 高和横向 8dp 内容间距。它们是两种实际变体，不应通过文档声称完全同形。

位选保存仅在已有有效结果、期号完整且未保存中时启用；保存中按钮显示局部进度。步骤内“下一位”仅在当前位已选数字时启用，最后一步提供“回看百位”。按钮焦点、按压与禁用反馈使用 Material 3，应用未指定网页 hover 动效。

### Chips

`ContextTag` 是不可点击的编辑状态标签，使用 `primaryContainer / onPrimaryContainer`、6dp 圆角与横向 8dp / 纵向 4dp 内边距；无需为纯标签编造 48dp 高度。单选已收录号码使用原生 `InputChip`，带移除图标和移除操作，保持号码三位显示。

### Cards / Containers

`WorkspacePanel` 是全宽、12dp 圆角的内容面，内部按行、列、分隔线组织；不内嵌一层层装饰卡片。`PanelHeading` 至少 48dp 高、左 16dp 右 8dp，为标题和行内操作留位置。`PaperPanel` 是已有的带 12dp 内间距的面，`emphasized` 才使用编辑浅底。

### Inputs / Fields

搜索期号或日期采用 12dp 圆角的原生 `OutlinedTextField`，搜索图标、占位提示和结果计数处在同一输入区。期号、批量号码、模板名与备注继续使用原生输入组件；单行、多行、错误和长度条件以实际调用点为准。输入焦点、光标、错误轮廓及 IME 行为交给 Material 3，不用 sidecar 的 HTML 控件实现 Android 行为。

### Navigation

主导航固定为首页、走势、选号、方案、彩报，五个等宽区域；图标 23dp、文字 12sp。选中态同时具有主色、较粗文字和底部短线，未选中态使用 `onSurfaceVariant`。`ChoiceRail` 把同组选择连接为一条平面控制，选中项带文字字重与下划线，使用原生 Tab 语义。两个组件都不依赖颜色单独表示选择。

页头与状态栏采用身份色，状态栏使用浅色图标；系统导航栏按主题使用内容面的颜色与图标明暗，安全区不挤占规定的内容高度。

### Position Accordion

三行步骤按百位、十位、个位排列。当前行使用编辑浅底；所有位置摘要持续存在；已完成的非当前位置显示勾选。仅当前位创建 `DigitGrid`，每键使用圆形选择状态，已选键额外显示勾选。切换位置仅改变展开区域，已选集合保留；活动位由 `rememberSaveable` 管理，新建或切换方法回到百位。

### Draw, History & Feedback

首页开奖区以身份色承托三颗反色数字圆底，期号、日期、更新动作、属性和详情入口保持邻近。试机号保持独立一行。历史区用合并表头与列对齐表达近期记录，当前主开奖不在后续五行中重复。失败信息留在所属区域，并保留已有数据与重试入口。

`WorkspaceSheet` 使用原生模态底部弹层、关闭按钮与 IME 间距。`dragHandle = null`，32×4dp 的静态把手放在内容开头，圆角 2dp、上方 12dp / 下方 4dp 间距；这个视觉标记不带 Material 3 把手 slot 的点击或 tooltip 语义。关闭按钮、系统返回与弹层拖动仍由原生弹层承担。保存反馈提供撤销、继续选号和查看方案。空态由图标、标题、说明和必要动作组成；彩报没有可选期号时引导刷新重试，有期号时才提示可换期，图片损坏时单独提示重新加载。不要把成功或命中反馈做成庆祝式装饰。上述局部修正已反映在代码，截图复核状态仍以本轮验收记录为准。

`.impeccable/design.json` 的 HTML/CSS 仅用于便携文档面板中的静态组件示意；双主题令牌、原生源位置和状态约束在其 native 扩展中记录。它不替代 Compose、原生字体、触控测量、屏幕截图或功能测试；示意中的“007”是前导零样例，不是当前开奖结果。

## Do's and Don'ts

### Do:

- Do 使用当前主题的原生颜色角色及对应 on 色，新增页面沿用同一套字号、间距和圆角。
- Do 保持至少 48dp 的操作范围；将说明和操作放在所属内容附近。
- Do 让三个位置摘要持续可见，仅展开当前位的十键数字区。
- Do 用真实历史数据检查密度、前导零、长文本和极端结果；完整号码进入明细。
- Do 为加载、空、失败、无网、重试及保存反馈保留明确状态；失败时保留已有内容。
- Do 在 360×800dp 与 393×851dp 的原生深浅色截图中检查核心任务可见性，并独立完成业务验证。

### Don't:

- Don't 恢复用户已否定的青蓝身份配色、苍白的整体效果、过大的留白或三套同时展开的数字键。
- Don't 将首页的开奖号优先构图推广成所有页面的大数字布局；其他页面按自己的任务组织。
- Don't 用装饰大卡片包裹每个短按钮，或通过缩小触控范围获得紧凑感。
- Don't 把第三方彩报原图、生成效果图或其文字当作 APK 资产、正式开奖或验收数据。
- Don't 把 HTML/CSS 检测、网页像素对比、构建通过或文档生成写成 Android 验收与用户审美认可。
