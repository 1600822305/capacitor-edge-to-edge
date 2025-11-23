# 发布指南 - capacitor-edge-to-edge

## 📋 发布前检查清单

- [x] package.json 配置完成
- [x] README.md 文档完整
- [x] LICENSE 文件存在
- [x] .npmignore 配置正确
- [ ] 所有代码已提交到 Git
- [ ] 测试通过
- [ ] 版本号正确

## 🚀 发布步骤

### 1. 安装依赖

```bash
npm install
```

### 2. 构建插件

```bash
npm run build
```

这会执行：
- 清理旧的构建文件
- 生成 API 文档
- 编译 TypeScript
- 打包为多种格式

### 3. 测试构建结果

检查 `dist/` 目录是否包含：
- `plugin.js` - UMD 格式
- `plugin.cjs.js` - CommonJS 格式
- `esm/` - ES Module 格式
- `docs.json` - API 文档

### 4. 登录 npm

如果还没有登录，运行：

```bash
npm login
```

输入你的 npm 用户名、密码和邮箱。

### 5. 检查包内容

预览将要发布的文件：

```bash
npm pack --dry-run
```

或者创建实际的 tarball：

```bash
npm pack
```

这会生成 `capacitor-edge-to-edge-0.0.1.tgz`，你可以解压检查内容。

### 6. 发布到 npm

首次发布：

```bash
npm publish
```

如果包名已存在，你可能需要使用作用域：

```bash
npm publish --access public
```

### 7. 验证发布

访问 npm 查看你的包：
```
https://www.npmjs.com/package/capacitor-edge-to-edge
```

测试安装：
```bash
npm install capacitor-edge-to-edge
```

## 📝 版本更新

发布新版本时：

```bash
# 补丁版本 (0.0.1 -> 0.0.2)
npm version patch

# 次版本 (0.0.1 -> 0.1.0)
npm version minor

# 主版本 (0.0.1 -> 1.0.0)
npm version major

# 发布
npm publish
```

## ⚠️ 常见问题

### 包名已存在

如果包名被占用，可以：
1. 使用作用域包名：`@your-username/capacitor-edge-to-edge`
2. 修改 package.json 中的 `name` 字段

### 发布失败 - 需要 2FA

如果你的 npm 账户启用了双因素认证：
```bash
npm publish --otp=123456
```
（将 123456 替换为你的认证码）

### 撤销发布

如果需要撤销（发布后 72 小时内）：
```bash
npm unpublish capacitor-edge-to-edge@0.0.1
```

注意：谨慎使用，撤销后该版本号不能再使用。

## 🎯 发布后

1. 在 GitHub 创建 Release
2. 更新 README 添加 npm badge
3. 在社区分享（Capacitor 官方论坛、Reddit 等）
4. 监控 issues 和 pull requests

## 📦 NPM Badges

在 README 中添加：

```markdown
[![npm version](https://badge.fury.io/js/capacitor-edge-to-edge.svg)](https://www.npmjs.com/package/capacitor-edge-to-edge)
[![npm downloads](https://img.shields.io/npm/dm/capacitor-edge-to-edge.svg)](https://www.npmjs.com/package/capacitor-edge-to-edge)
```
