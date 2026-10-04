## 基地

- [项目地址](https://gitee.com/dtstack/qd)
- [文档地址](https://dtstack.gitee.io/qd/)

### 技术栈
hutool
mybatis-plus
p6spy
sa-token
mapstruct

### 使用步骤
下载easyCode插件，导入easyCode配置文件`docs/easyCode/EasyCodeConfig.json`，在数据库表上点击生成即可


### 规范
#### 枚举值规范
目录：`~/qd/common/constant/enums`
- 枚举值命名使用大写字母和下划线分隔单词，例如：`USER_STATUS_ACTIVE`。
- 枚举值应具有描述性，能够清晰表达其含义。
- 枚举值应避免使用缩写，除非是广泛认可的缩写词。
- 枚举值应保持唯一性，避免重复定义相同的值。
- 枚举值应按照逻辑顺序排列，便于查找和维护。
- 每个枚举值应包含简要的注释，说明其用途和含义。
- 必须包含value和label字段，value用于存储实际值，label用于存储显示名称。
