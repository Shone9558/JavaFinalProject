JavaFinalProject 執行說明（更新版）

本版已更新：
1. 移除底部多餘的「加入收藏」與「刪除收藏」按鈕。
   商品卡片上的「收藏 / 已收藏」按鈕仍保留，收藏與取消收藏功能也保留。
2. 登入後右上方新增「登出」按鈕。
   點擊後會回到原本的登入 / 註冊 / 訪客進入畫面。
3. 保留前一版修正：搜尋防連點、登入資料隔離、全平台搜尋狀態固定化。

PowerShell 快速執行：
.compile.ps1
.run.ps1

如果 PowerShell 不允許執行 ps1，可以用：
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

手動編譯：
$files = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp ".;jsoup-1.22.1.jar;gson-2.10.1.jar;sqlite-jdbc-3.53.1.0.jar;jfreechart-1.5.4.jar;webp-imageio-0.1.6.jar" -d . $files

手動執行：
java "-Dfile.encoding=UTF-8" -cp ".;jsoup-1.22.1.jar;gson-2.10.1.jar;sqlite-jdbc-3.53.1.0.jar;jfreechart-1.5.4.jar;webp-imageio-0.1.6.jar" ntou.cs.java2026.Main
