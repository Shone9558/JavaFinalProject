$files = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -cp ".;jsoup-1.22.1.jar;gson-2.10.1.jar;sqlite-jdbc-3.53.1.0.jar;jfreechart-1.5.4.jar;webp-imageio-0.1.6.jar" -d . $files
