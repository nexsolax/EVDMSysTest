import React, { useState } from 'react';
import { X, Upload, Image as ImageIcon, Check } from 'lucide-react';
import { publicInventoryAPI } from '../services/api';
import toast from 'react-hot-toast';
import './modals/Modal.css';

const ImageUploadModal = ({ isOpen, onClose, onSuccess, vehicleId, vehicleName }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [uploading, setUploading] = useState(false);

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith('image/')) {
        toast.error('Vui lòng chọn file hình ảnh');
        return;
      }
      
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        toast.error('Kích thước file không được vượt quá 5MB');
        return;
      }

      setSelectedFile(file);
      
      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => setPreview(e.target.result);
      reader.readAsDataURL(file);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile || !vehicleId) {
      toast.error('Vui lòng chọn file và xe');
      return;
    }

    try {
      setUploading(true);
      
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('vehicleId', vehicleId);

      const response = await publicInventoryAPI.uploadImage(formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.data) {
        toast.success('Upload hình ảnh thành công!');
        onSuccess?.(response.data);
        handleClose();
      }
    } catch (error) {
      console.error('Upload error:', error);
      toast.error('Lỗi khi upload hình ảnh: ' + (error.response?.data?.message || error.message));
    } finally {
      setUploading(false);
    }
  };

  const handleClose = () => {
    setSelectedFile(null);
    setPreview(null);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content image-upload-modal">
        <div className="modal-header">
          <h2 className="modal-title">
            <ImageIcon className="modal-icon" />
            Upload hình ảnh xe
          </h2>
          <button className="modal-close" onClick={handleClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-body">
          <div className="upload-info">
            <p><strong>Xe:</strong> {vehicleName}</p>
            <p><strong>ID:</strong> {vehicleId}</p>
          </div>

          <div className="upload-area">
            <input
              type="file"
              id="image-upload"
              accept="image/*"
              onChange={handleFileSelect}
              style={{ display: 'none' }}
            />
            
            <label htmlFor="image-upload" className="upload-button">
              <Upload className="upload-icon" />
              <span>Chọn hình ảnh</span>
            </label>

            {preview && (
              <div className="image-preview">
                <img src={preview} alt="Preview" />
                <div className="preview-info">
                  <p><strong>File:</strong> {selectedFile?.name}</p>
                  <p><strong>Kích thước:</strong> {(selectedFile?.size / 1024 / 1024).toFixed(2)} MB</p>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="modal-footer">
          <button 
            className="btn btn-secondary" 
            onClick={handleClose}
            disabled={uploading}
          >
            Hủy
          </button>
          <button 
            className="btn btn-primary" 
            onClick={handleUpload}
            disabled={!selectedFile || uploading}
          >
            {uploading ? (
              <>
                <div className="spinner"></div>
                Đang upload...
              </>
            ) : (
              <>
                <Check size={16} />
                Upload
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ImageUploadModal;
