const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const path = require('path');
const fs = require('fs');

const isDev = !app.isPackaged;
const { shell } = require('electron');
const { spawn } = require('child_process'); 

let mainWindow;

function createWindow() {
  // 创建浏览器窗口
  mainWindow = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js') // 确保有preload脚本
    },
    show: false
  });

  // // 加载应用
  // if (isDev) {
  //   mainWindow.loadURL('http://localhost:5173');
  //   // mainWindow.webContents.openDevTools();
  // } else {
  //   mainWindow.loadFile(path.join(__dirname, '../vue-frontend/dist/index.html'));
  // }

    // 开启开发者工具以便调试
  mainWindow.webContents.openDevTools();

  // 监听加载失败事件
  mainWindow.webContents.on('did-fail-load', (event, errorCode, errorDescription) => {
    console.log('Failed to load:', errorCode, errorDescription);
  });

  mainWindow.loadFile('static/index.html');

  // 准备就绪时显示窗口
  mainWindow.once('ready-to-show', () => {
    mainWindow.show();
    setupDownloadHandler(mainWindow);
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}


// 添加打开文件处理
ipcMain.handle('open-file', async (event, filePath) => {
  try {
    await shell.openPath(filePath);
    return { success: true };
  } catch (error) {
    console.error('打开文件失败:', error);
    return { success: false, error: error.message };
  }
});

// 添加文件选择对话框处理
ipcMain.handle('select-pdf-files', async () => {
  try {
    const result = await dialog.showOpenDialog(mainWindow, { // 添加mainWindow作为父窗口
      properties: ['openFile', 'multiSelections'],
      filters: [
        { name: 'PDF Files', extensions: ['pdf'] }
      ],
      title: '选择PDF文件'
    });
    
    if (!result.canceled) {
      return result.filePaths; // 返回完整的文件路径数组
    }
    return [];
  } catch (error) {
    console.error('文件选择错误:', error);
    return [];
  }
});

app.whenReady().then(()=>{
startBackend();
  createWindow();
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});



// 在创建窗口后添加下载处理
function setupDownloadHandler(mainWindow) {
  // 监听下载事件
  mainWindow.webContents.session.on('will-download', (event, item, webContents) => {
    // 获取文件名
    const fileName = item.getFilename();
    const filePath = path.join(app.getPath('downloads'), fileName);
    
    // 设置保存路径
    item.setSavePath(filePath);
    
    // 监听下载进度
    item.on('updated', (event, state) => {
      if (state === 'interrupted') {
        console.log('下载已中断');
      } else if (state === 'progressing') {
        if (item.isPaused()) {
          console.log('下载已暂停');
        } else {
          console.log(`下载进度: ${((item.getReceivedBytes() / item.getTotalBytes()) * 100).toFixed(2)}%`);
        }
      }
    });
    
    // 下载完成
    item.once('done', (event, state) => {
      if (state === 'completed') {
        console.log('下载完成:', filePath);
        // 通知渲染进程下载完成
        mainWindow.webContents.send('download-complete', {
          success: true,
          filePath: filePath,
          fileName: fileName
        });
      } else {
        console.log('下载失败:', state);
        mainWindow.webContents.send('download-complete', {
          success: false,
          error: `下载失败: ${state}`
        });
      }
    });
  });
}


// 添加保存文件对话框
ipcMain.handle('save-file-dialog', async (event, buffer, defaultFileName) => {
  try {
    const result = await dialog.showSaveDialog(mainWindow, {
      defaultPath: defaultFileName,
    //   filters: [
    //     { name: 'PDF Files', extensions: ['pdf'] },
    //     { name: 'ZIP Files', extensions: ['zip'] },
    //     { name: 'All Files', extensions: ['*'] }
    //   ]
    });
    
    if (!result.canceled && result.filePath) {
      // 将 buffer 写入文件
      fs.writeFileSync(result.filePath, Buffer.from(buffer));
      return { success: true, filePath: result.filePath };
    } else {
      return { success: false, canceled: true };
    }
  } catch (error) {
    return { success: false, error: error.message };
  }
});


// 启动 Spring Boot 后端JAR函数
function startBackend() {
  // 假设你的JAR文件就在Electron项目根目录下
  const jarPath = path.join(__dirname, 'pdfutil-1.0-SNAPSHOT.jar'); // 请替换为你的JAR文件名
  
  // 检查JAR文件是否存在
  if (!fs.existsSync(jarPath)) {
    console.error('JAR file not found:', jarPath);
    return;
  }

 // 设置环境变量
  const env = {
    ...process.env, // 继承当前环境
    LANG: 'zh_CN.UTF-8',
    LC_ALL: 'zh_CN.UTF-8',
    LC_CTYPE: 'zh_CN.UTF-8',
    JAVA_TOOL_OPTIONS: '-Duser.language=zh -Duser.region=CN -Dfile.encoding=UTF-8'
  };

  javaProcess = spawn('java', [
    '-Duser.language=zh',
    '-Duser.region=CN', 
    '-Dfile.encoding=UTF-8',
    '-jar', jarPath
  ], {
    env: env,
    stdio: ['pipe', 'pipe', 'pipe']
  });

  javaProcess.stdout.on('data', (data) => {
    console.log('后端输出:', data.toString().trim());
  });

  javaProcess.stderr.on('data', (data) => {
    const error = data.toString().trim();
    console.error('后端错误:', error);
  });
}