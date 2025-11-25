const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
  // 文件选择
  selectPdfFiles: () => ipcRenderer.invoke('select-pdf-files'),
  // 保存文件对话框
  saveFileDialog: (buffer, defaultFileName) => ipcRenderer.invoke('save-file-dialog', buffer, defaultFileName),
  // 下载完成事件
  onDownloadComplete: (callback) => ipcRenderer.on('download-complete', callback)
});