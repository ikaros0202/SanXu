# 本项目使用的 UI skill

2026-10-02 按用户要求搜索并安装；原 Lucky3D 目录不修改。

## 本轮新增

- 项目：pbakaus/impeccable，https://github.com/pbakaus/impeccable
- GitHub API 核对时星标：73,903，大于15,000。
- 最近一周窗口：2026-09-25—2026-10-02（北京时间）；最新主分支提交2026-10-02 10:34:09，符合。
- 提交：508d7e8955de3b3caf2d8676e85206723d41a887，Release skill 4.5.0 (#913)。固定该提交安装，未使用浮动分支。
- 安装位置：C:/Users/jiqin/.codex/skills/impeccable。
- 安装命令：`python C:/Users/jiqin/.codex/skills/.system/skill-installer/scripts/install-skill-from-github.py --repo pbakaus/impeccable --ref 508d7e8955de3b3caf2d8676e85206723d41a887 --path .agents/skills/impeccable`。
- 安装成功；已执行 `scripts/impeccable.cmd context --target app/src/main/kotlin/com/sanxu`，直接阅读并应用 Android、Operate、新视觉工作及效果图流程。
- 新安装 skill 下轮自动发现；本轮通过读取安装文件和执行其工具使用。
- 仓库许可证 Apache-2.0；安装 skill 不改变本应用技术栈。

## 同时核对的候选

| 项目 | 星标 | 最新提交（UTC） | 取舍 |
|---|---:|---|---|
| anthropics/skills | 179,368 | 2026-09-29 02:20:03 | 收录 frontend-design，仓库活跃；该次最新提交属于 API skill，不能当作设计 skill 本身更新 |
| nextlevelbuilder/ui-ux-pro-max-skill | 132,433 | 2026-09-27 11:30:41 | 已安装，上一轮已使用；本轮新增 Impeccable 提供不同的设计审视流程 |

星标为本次读取时的快照，不是永久不变的数值。项目活跃依据提交时间，不以网页抓取日期替代。

用户禁止的 oiloil-ui-ux-guide、wireframe-spec 继续不使用。

## 原生适配与验收范围

Impeccable网页comp流水线的COMPS阶段要求仅一张approved图片，本任务同时确认了首页和B选号两张页面，并再次明确要求缩小组件；没有强制绕过该检查或宣称网页门禁通过。采用其Android/Operate规则，以独立原生评审、dp区域规格和整屏边界测试验收。网页HTML/CSS检测器不运行。收尾文档由独立documenter整理，最终评审记录单独保存。
