SQLite 使用說明
================

本專案已加入 SQLite 資料庫程式碼，資料庫檔名為 shopping_tracker.db。

Java 本身沒有內建 SQLite JDBC Driver，因此如果要啟用 SQLite，請將 sqlite-jdbc.jar 放到專案根目錄，並在編譯/執行 classpath 加入它。

Windows PowerShell 範例：

javac -encoding UTF-8 -cp ".;jsoup-1.22.1.jar;gson-2.10.1.jar;sqlite-jdbc.jar" -d . $files

java "-Dfile.encoding=UTF-8" -cp ".;jsoup-1.22.1.jar;gson-2.10.1.jar;sqlite-jdbc.jar" ntou.cs.java2026.Main

如果沒有 sqlite-jdbc.jar，程式仍可正常執行，會自動使用原本的 favorites.csv 與 search_history.txt 儲存。
