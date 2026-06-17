# scripts/ 工具脚本

数据库词库生成与维护脚本。**不属于前后端源码**，仅用于数据初始化与重建。

## 文件清单

| 文件 | 用途 |
|------|------|
| `image_pool.py` | 1500+ 张 Unsplash 图片池，按 8 大语义分类（动物/食物/动作/场所/物品/自然/抽象/情绪）组织 |
| `word_to_category.py` | 3000+ 常用英文单词到图片分类的映射表 |
| `gen_all_books.py` | 主生成脚本：读取词库 JSON + 图片池 → 生成 word/word_meaning/word_sentence 三段 SQL |
| `merge_to_smartvocab.py` | 把生成的 SQL 合并进 `db/smartvocab.sql`（幂等） |
| `import_smartvocab.py` | 用 pymysql 直接把 SQL 导入 MySQL（无需 mysql CLI） |
| `verify_data.py` | 词库完整性检查（词数对齐、释义/例句覆盖率、音标抽样、图片 URL 唯一性） |

## 使用流程

```bash
# 1. 生成 5 本词书的 SQL（写入 scripts/all_books.sql 临时文件）
python gen_all_books.py

# 2. 合并进 db/smartvocab.sql（UTF-8 BOM 编码）
python merge_to_smartvocab.py

# 3. 导入到 MySQL
python import_smartvocab.py

# 4. 验证数据完整性
python verify_data.py
```

## 词库数据来源

- CET4 / CET6 / 考研 / 雅思：`kajweb/dict` 仓库（JSON 格式，含音标/释义/例句）
- 数据通过 web 抓取得到，未随仓库发布；如需重跑完整生成需先获取原始词库

## 维护建议

- 图片 URL 失效时：编辑 `image_pool.py` 替换对应分类的 photo ID
- 新词类映射：编辑 `word_to_category.py` 添加新词
- 重新生成会清空 book 1/2/3/6/7 的数据，原 25 个示例单词（id 1-25）保留
