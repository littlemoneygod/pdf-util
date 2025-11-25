<template>
  <div class="multi-pdf-viewer">
    <div class="layout-container">
      <!-- 左侧文件管理区域 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>PDF 文件</h3>
          <div class="file-input-container">
            <label class="file-label">
              <input
                type="file"
                @change="handleFileSelect"
                accept=".pdf"
                class="file-input"
                multiple
              >
              <span class="file-button">
                <span class="button-icon">+</span>
                选择 PDF 文件
              </span>
            </label>
            <button 
              v-if="files.length > 0" 
              @click="clearAllFiles" 
              class="clear-button"
            >
              <span class="button-icon">×</span>
              清空列表
            </button>
          </div>
        </div>

        <!-- 文件列表 -->
        <div class="file-list-container" v-if="files.length > 0">
          <div class="file-list">
            <div 
              v-for="file in files" 
              :key="file.id"
              :class="['file-item', { active: currentFileId === file.id }]"
              @click="switchFile(file.id)"
            >
              <div class="file-icon">📄</div>
              <div class="file-info">
                <div class="file-name">{{ file.name }}</div>
                <div class="file-size">{{ formatFileSize(file.size) }}</div>
              </div>
              <button 
                class="file-remove"
                @click.stop="removeFile(file.id)"
                title="移除文件"
              >
                ×
              </button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <div class="empty-content">
            <div class="empty-icon">📚</div>
            <h4>暂无文件</h4>
            <p>点击"选择 PDF 文件"按钮添加文件</p>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="file-stats" v-if="files.length > 0">
          <div class="stat-item">
            <span class="stat-label">文件数量:</span>
            <span class="stat-value">{{ files.length }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">当前文件:</span>
            <span class="stat-value">{{ currentFileIndex + 1 }}/{{ files.length }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧PDF预览区域 -->
      <div class="preview-area">
        <div class="preview-header">
          <h3>PDF 预览</h3>
          <div class="preview-controls" v-if="currentFile">
            <span class="current-file-name">{{ currentFile.name }}</span>
            <div class="control-buttons">
              <button 
                @click="prevFile" 
                :disabled="currentFileIndex <= 0"
                class="control-btn"
                title="上一个文件"
              >
                ‹
              </button>
              <button 
                @click="nextFile" 
                :disabled="currentFileIndex >= files.length - 1"
                class="control-btn"
                title="下一个文件"
              >
                ›
              </button>
            </div>
          </div>
        </div>

        <div class="preview-content">
          <div v-if="currentFileUrl" class="pdf-container">
            <iframe
              :src="currentFileUrl"
              class="pdf-frame"
              frameborder="0"
            ></iframe>
          </div>
          
          <div v-else class="preview-empty">
            <div class="preview-empty-content">
              <div class="preview-icon">👆</div>
              <h3>选择文件进行预览</h3>
              <p>从左侧文件列表中选择一个 PDF 文件进行查看</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部操作按钮 -->
    <div class="footer" v-if="files.length > 0">
      <div class="button-group">
        <button 
          v-if="files.length > 1"
          @click="mergePdfs" 
          class="action-button merge-button"
          :disabled="merging"
        >
          <span v-if="merging" class="button-loading">
            <span class="spinner"></span>
            合并中...
          </span>
          <span v-else class="button-text">
            <span class="button-icon">🔄</span>
            合并 PDF
          </span>
        </button>

        <button 
          @click="showSplitDialog = true" 
          class="action-button split-button"
          :disabled="splitting"
        >
          <span v-if="splitting" class="button-loading">
            <span class="spinner"></span>
            拆分中...
          </span>
          <span v-else class="button-text">
            <span class="button-icon">✂️</span>
            拆分 PDF
          </span>
        </button>
      </div>
    </div>

    <!-- 拆分PDF弹框 -->
    <div v-if="showSplitDialog" class="modal-overlay" @click="showSplitDialog = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>拆分 PDF</h3>
          <button class="modal-close" @click="showSplitDialog = false">×</button>
        </div>
        
        <div class="modal-body">
          <div class="split-instructions">
            <p>请输入要拆分的页码，每行一个数字：</p>
            <small>例如：输入 3 表示在第3页后拆分</small>
          </div>
          
          <div class="page-inputs">
            <div 
              v-for="(input, index) in splitPages" 
              :key="index"
              class="page-input-row"
            >
              <input
                type="number"
                v-model.number="splitPages[index]"
                placeholder="输入页码"
                min="1"
                class="page-input"
                @keypress="validateNumber"
              >
              <button 
                v-if="splitPages.length > 1"
                @click="removePageInput(index)"
                class="remove-input-btn"
                title="删除"
              >
                ×
              </button>
            </div>
          </div>
          
          <button @click="addPageInput" class="add-input-btn">
            <span class="add-icon">+</span>
            添加页码
          </button>
          
          <div class="current-file-info" v-if="currentFile">
            <p>当前文件: <strong>{{ currentFile.name }}</strong></p>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="showSplitDialog = false" class="btn-cancel">取消</button>
          <button 
            @click="splitPdf" 
            class="btn-confirm"
            :disabled="!canSplit"
          >
            确定拆分
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onUnmounted, computed } from 'vue';
import axios from 'axios';
import  apiClient  from '../config/api';

export default {
  name: 'MultiPdfViewer',
  setup() {
    const files = ref([]);
    const currentFileId = ref(null);
    const merging = ref(false);
    const splitting = ref(false);
    const showSplitDialog = ref(false);
    const splitPages = ref([1]); // 默认一个输入框

    // 生成唯一ID
    const generateId = () => {
      return Date.now().toString(36) + Math.random().toString(36).substr(2);
    };

    // 计算属性
    const currentFile = computed(() => {
      return files.value.find(f => f.id === currentFileId.value) || null;
    });

    const currentFileUrl = computed(() => {
      return currentFile.value ? currentFile.value.url : null;
    });

    const currentFileIndex = computed(() => {
      return files.value.findIndex(f => f.id === currentFileId.value);
    });

    // 检查是否可以拆分
    const canSplit = computed(() => {
      return splitPages.value.some(page => page > 0);
    });

    // 文件大小格式化
    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    // 处理文件选择
    const handleFileSelect = (event) => {
      const selectedFiles = Array.from(event.target.files);
      const pdfFiles = selectedFiles.filter(file => file.type === 'application/pdf');
      
      if (pdfFiles.length === 0) {
        alert('请选择有效的 PDF 文件');
        return;
      }

      pdfFiles.forEach(file => {
        const fileId = generateId();
        const fileUrl = URL.createObjectURL(file);
        
        files.value.push({
          id: fileId,
          name: file.name,
          file: file,
          size: file.size,
          url: fileUrl
        });
      });

      if (!currentFileId.value && files.value.length > 0) {
        currentFileId.value = files.value[files.value.length - 1].id;
      }

      event.target.value = '';
    };

    // 切换文件
    const switchFile = (fileId) => {
      currentFileId.value = fileId;
    };

    // 上一个文件
    const prevFile = () => {
      if (currentFileIndex.value > 0) {
        currentFileId.value = files.value[currentFileIndex.value - 1].id;
      }
    };

    // 下一个文件
    const nextFile = () => {
      if (currentFileIndex.value < files.value.length - 1) {
        currentFileId.value = files.value[currentFileIndex.value + 1].id;
      }
    };

    // 移除单个文件
    const removeFile = (fileId) => {
      const fileIndex = files.value.findIndex(f => f.id === fileId);
      if (fileIndex !== -1) {
        URL.revokeObjectURL(files.value[fileIndex].url);
        files.value.splice(fileIndex, 1);
        
        if (currentFileId.value === fileId) {
          if (files.value.length > 0) {
            const newIndex = Math.min(fileIndex, files.value.length - 1);
            currentFileId.value = files.value[newIndex].id;
          } else {
            currentFileId.value = null;
          }
        }
      }
    };

    // 清除所有文件
    const clearAllFiles = () => {
      files.value.forEach(file => {
        URL.revokeObjectURL(file.url);
      });
      files.value = [];
      currentFileId.value = null;
    };



    // 拆分PDF相关方法
    const addPageInput = () => {
      splitPages.value.push('');
    };

    const removePageInput = (index) => {
      if (splitPages.value.length > 1) {
        splitPages.value.splice(index, 1);
      }
    };

    const validateNumber = (event) => {
      const keyCode = event.keyCode || event.which;
      const keyValue = String.fromCharCode(keyCode);
      if (!/^\d+$/.test(keyValue)) {
        event.preventDefault();
      }
    };

   // 修改合并PDF方法
const mergePdfs = async () => {
  if (files.value.length < 2) {
    alert('请至少选择2个PDF文件进行合并');
    return;
  }

  merging.value = true;

  try {
    const formData = new FormData();
    
    files.value.forEach((fileObj, index) => {
      formData.append('files', fileObj.file, fileObj.name);
    });
    
    formData.append('outputFileName', `merged_${Date.now()}.pdf`);
    formData.append('mergeOrder', JSON.stringify(files.value.map(f => f.id)));

    const response = await apiClient.post('/api/pdf/merge', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 120000,
      responseType: 'arraybuffer' // 改为 arraybuffer
    });

    // 在 Electron 中使用文件对话框保存
    if (window.electronAPI) {
      const result = await window.electronAPI.saveFileDialog(
        response.data,
        `merged_${Date.now()}.pdf`
      );
      
      if (result.success) {
        alert(`PDF合并成功！文件已保存到: ${result.filePath}`);
      } else if (result.canceled) {
        alert('用户取消了保存');
      } else {
        alert(`保存失败: ${result.error}`);
      }
    } else {
      // 备用方案：在浏览器中下载
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `merged_${Date.now()}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      alert('PDF合并成功！文件已开始下载。');
    }
    
  } catch (error) {
    console.error('合并PDF失败:', error);
    alert('合并失败，请检查后端服务是否正常运行');
  } finally {
    merging.value = false;
  }
};

// 修改拆分PDF方法
const splitPdf = async () => {
  if (!currentFile.value) {
    alert('请先选择一个PDF文件');
    return;
  }

  // 验证输入
  const validPages = splitPages.value
    .map(page => parseInt(page))
    .filter(page => !isNaN(page) && page > 0)
    .sort((a, b) => a - b);

  if (validPages.length === 0) {
    alert('请输入有效的页码');
    return;
  }

  splitting.value = true;

  try {
    const formData = new FormData();
    formData.append('file', currentFile.value.file);
    formData.append('splitPages', validPages);
    formData.append('outputFileName', `split_${Date.now()}`);

    const response = await apiClient.post('/api/pdf/split', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 120000,
      responseType: 'arraybuffer' // 改为 arraybuffer
    });

    // 在 Electron 中使用文件对话框保存
    if (window.electronAPI) {
      const result = await window.electronAPI.saveFileDialog(
        response.data,
        `split_${Date.now()}.zip`
      );
      
      if (result.success) {
        alert(`PDF拆分成功！文件已保存到: ${result.filePath}`);
        showSplitDialog.value = false;
      } else if (result.canceled) {
        alert('用户取消了保存');
      } else {
        alert(`保存失败: ${result.error}`);
      }
    } else {
      // 备用方案：在浏览器中下载
      const blob = new Blob([response.data], { type: 'application/zip' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `split_${Date.now()}.zip`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      alert('PDF拆分成功！文件已开始下载。');
      showSplitDialog.value = false;
    }
    
  } catch (error) {
    console.error('拆分PDF失败:', error);
    alert('拆分失败，请检查后端服务是否正常运行');
  } finally {
    splitting.value = false;
  }
};

    // 组件卸载时清理URL
    onUnmounted(() => {
      files.value.forEach(file => {
        URL.revokeObjectURL(file.url);
      });
    });

    return {
      files,
      currentFileId,
      currentFile,
      currentFileUrl,
      currentFileIndex,
      merging,
      splitting,
      showSplitDialog,
      splitPages,
      canSplit,
      handleFileSelect,
      switchFile,
      prevFile,
      nextFile,
      removeFile,
      clearAllFiles,
      mergePdfs,
      addPageInput,
      removePageInput,
      validateNumber,
      splitPdf,
      formatFileSize
    };
  }
};
</script>

<style scoped>
/* 基础布局 */
.multi-pdf-viewer {
  height: 100vh;
  width: 100vw;
  background: #f8f9fa;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
}

.layout-container {
  display: flex;
  flex: 1;
  background: #e9ecef;
  overflow: hidden;
  min-height: 0;
}

/* 左侧边栏 */
.sidebar {
  width: 350px;
  background: white;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e9ecef;
  min-height: 0;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
  flex-shrink: 0;
}

.sidebar-header h3 {
  margin: 0 0 1rem 0;
  color: #2c3e50;
  font-size: 1.25rem;
}

.file-input-container {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.file-label {
  display: block;
}

.file-input {
  display: none;
}

.file-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #007bff;
  color: white;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
  border: none;
  justify-content: center;
  text-align: center;
}

.file-button:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

.clear-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
  justify-content: center;
  text-align: center;
}

.clear-button:hover {
  background: #545b62;
  transform: translateY(-1px);
}

.button-icon {
  font-size: 1.2rem;
  font-weight: bold;
}

/* 文件列表 */
.file-list-container {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.file-list {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
  min-height: 0;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  margin-bottom: 0.5rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: white;
  flex-shrink: 0;
}

.file-item:hover {
  border-color: #007bff;
  background: #f8f9ff;
}

.file-item.active {
  border-color: #007bff;
  background: #007bff;
  color: white;
}

.file-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.file-name {
  font-weight: 500;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 0.75rem;
  opacity: 0.7;
}

.file-item.active .file-size {
  opacity: 0.9;
}

.file-remove {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 3px;
  font-size: 1.2rem;
  line-height: 1;
  opacity: 0.6;
  transition: all 0.2s;
  flex-shrink: 0;
}

.file-remove:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.2);
}

.file-item:not(.active) .file-remove {
  color: #6c757d;
}

.file-item:not(.active) .file-remove:hover {
  background: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  overflow: hidden;
}

.empty-content {
  text-align: center;
  color: #6c757d;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-content h4 {
  margin: 0 0 0.5rem 0;
  color: #495057;
}

.empty-content p {
  margin: 0;
  font-size: 0.9rem;
}

/* 统计信息 */
.file-stats {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-label {
  font-size: 0.875rem;
  color: #6c757d;
}

.stat-value {
  font-weight: 600;
  color: #495057;
}

/* 右侧预览区域 */
.preview-area {
  flex: 1;
  background: white;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
  flex-shrink: 0;
}

.preview-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.25rem;
}

.preview-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.current-file-name {
  font-weight: 500;
  color: #495057;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.control-buttons {
  display: flex;
  gap: 0.25rem;
}

.control-btn {
  background: white;
  border: 1px solid #dee2e6;
  color: #495057;
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1.2rem;
  line-height: 1;
}

.control-btn:hover:not(:disabled) {
  background: #007bff;
  border-color: #007bff;
  color: white;
}

.control-btn:disabled {
  background: #f8f9fa;
  color: #adb5bd;
  cursor: not-allowed;
}

/* 预览内容 */
.preview-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.pdf-container {
  flex: 1;
  overflow: hidden;
  background: #f8f9fa;
  display: flex;
  min-height: 0;
}

.pdf-frame {
  width: 100%;
  height: 100%;
  border: none;
  min-height: 0;
}

.preview-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  overflow: hidden;
}

.preview-empty-content {
  text-align: center;
  color: #6c757d;
  max-width: 300px;
}

.preview-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.preview-empty-content h3 {
  margin: 0 0 0.5rem 0;
  color: #495057;
}

.preview-empty-content p {
  margin: 0;
  font-size: 0.9rem;
}

/* 底部操作按钮 */
.footer {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
}

.button-group {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.action-button {
  padding: 1rem 2rem;
  border: none;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 160px;
  justify-content: center;
}

.action-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.action-button:active:not(:disabled) {
  transform: translateY(0);
}

.action-button:disabled {
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  opacity: 0.7;
}

.merge-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.split-button {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.button-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.button-text {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.button-icon {
  font-size: 1.2rem;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 弹框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.modal-close:hover {
  background: #e9ecef;
  color: #495057;
}

.modal-body {
  padding: 1.5rem;
  max-height: 400px;
  overflow-y: auto;
}

.split-instructions {
  margin-bottom: 1.5rem;
}

.split-instructions p {
  margin: 0 0 0.5rem 0;
  color: #495057;
}

.split-instructions small {
  color: #6c757d;
}

.page-inputs {
  margin-bottom: 1rem;
}

.page-input-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.page-input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 1rem;
}

.page-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.remove-input-btn {
  background: #dc3545;
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.remove-input-btn:hover {
  background: #c82333;
}

.add-input-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  margin-bottom: 1.5rem;
}

.add-input-btn:hover {
  background: #218838;
}

.add-icon {
  font-size: 1.2rem;
  font-weight: bold;
}

.current-file-info {
  background: #e7f3ff;
  border: 1px solid #b3d9ff;
  border-radius: 6px;
  padding: 1rem;
  margin-top: 1rem;
}

.current-file-info p {
  margin: 0.25rem 0;
  color: #0066cc;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  background: #f8f9fa;
}

.btn-cancel {
  background: #6c757d;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.btn-cancel:hover {
  background: #545b62;
}

.btn-confirm {
  background: #007bff;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.btn-confirm:hover:not(:disabled) {
  background: #0056b3;
}

.btn-confirm:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 滚动条 */
.file-list::-webkit-scrollbar {
  width: 6px;
}

.file-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.file-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.file-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar {
    width: 300px;
  }
}

@media (max-width: 768px) {
  .layout-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: 40%;
  }
  
  .preview-area {
    height: 60%;
  }
  
  .preview-controls {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-end;
  }
  
  .current-file-name {
    max-width: 200px;
  }

  .footer {
    bottom: 1rem;
  }

  .button-group {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .action-button {
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    min-width: 140px;
  }
  
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
}
</style>