$baseUrl = 'http://localhost:8080/api'
$body = '{"email":"demo@smartvocab.com","password":"123456"}'
$login = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -ContentType 'application/json;charset=utf-8' -Body $body
$token = $login.data.token
Write-Host "=== 登录测试 ==="
Write-Host ("user: " + $login.data.user.email)
Write-Host ("nick: " + $login.data.user.nickname)

$headers = @{ Authorization = "Bearer $token" }

Write-Host "`n=== 词书列表 ==="
$books = Invoke-RestMethod -Uri "$baseUrl/books" -Headers $headers
$books.data | Select-Object id, name, word_count, category | Format-Table -AutoSize

Write-Host "`n=== 词书 1 (CET4) 第 1 页 ==="
$words = Invoke-RestMethod -Uri "$baseUrl/words/book/1?current=1&size=5" -Headers $headers
$words.data.records | Select-Object id, spelling, phonetic_us, image_url | Format-Table -AutoSize | Out-String | Write-Host
Write-Host ("total: " + $words.data.total)

Write-Host "`n=== 词书 4 (高中) 第 1 页 ==="
$words4 = Invoke-RestMethod -Uri "$baseUrl/words/book/4?current=1&size=3" -Headers $headers
$words4.data.records | Select-Object id, spelling, phonetic_us | Format-Table -AutoSize | Out-String | Write-Host
Write-Host ("total: " + $words4.data.total)

Write-Host "`n=== 词书 5 (初中) 第 1 页 ==="
$words5 = Invoke-RestMethod -Uri "$baseUrl/words/book/5?current=1&size=3" -Headers $headers
$words5.data.records | Select-Object id, spelling, phonetic_us | Format-Table -AutoSize | Out-String | Write-Host
Write-Host ("total: " + $words5.data.total)

Write-Host "`n=== 仪表盘 ==="
$dash = Invoke-RestMethod -Uri "$baseUrl/stat/dashboard" -Headers $headers
$dash.data | Format-List

Write-Host "`n=== 学习模块 (book 1, 5 词) ==="
$learn = Invoke-RestMethod -Uri "$baseUrl/learn/today?bookId=1&size=5" -Headers $headers
$learn.data | Select-Object id, spelling, phonetic_us, image_url | Format-Table -AutoSize | Out-String | Write-Host
