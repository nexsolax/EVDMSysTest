import React, { useState, useEffect } from 'react';
import { X, Upload, Image as ImageIcon, Search, Filter, Trash2, Move, Edit, Eye, Download } from 'lucide-react';
import { imageAPI } from '../../services/api';
import './Modal.css';

const ImageManagementModal = ({ isOpen, onClose, onImageSelect }) => {
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [images, setImages] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedImages, setSelectedImages] = useState([]);
  const [uploadInfo, setUploadInfo] = useState(null);

  useEffect(() => {
    if (isOpen) {
      loadImages();
      loadUploadInfo();
    }
  }, [isOpen, selectedCategory, currentPage]);

  const loadImages = async () => {
    try {
      setLoading(true);
      setError('');
      
      const response = await imageAPI.listImages(selectedCategory, currentPage, 20);
      const data = response.data;
      
      setImages(data.images || []);
      setTotalPages(data.totalPages || 0);
      setCategories(data.categories || []);
    } catch (error) {
      console.error('Error loading images:', error);
      setError('Không thể tải danh sách hình ảnh');
    } finally {
      setLoading(false);
    }
  };

  const loadUploadInfo = async () => {
    try {
      const response = await imageAPI.getUploadInfo();
      setUploadInfo(response.data);
    } catch (error) {
      console.error('Error loading upload info:', error);
    }
  };

  const handleUpload = async (files, category) => {
    if (!files || files.length === 0) return;

    try {
      setUploading(true);
      setError('');

      const response = await imageAPI.uploadMultipleImages(Array.from(files), category);
      
      if (response.data) {
        setSuccess(`Upload thành công ${files.length} hình ảnh`);
        loadImages(); // Reload images
      }
    } catch (error) {
      console.error('Error uploading images:', error);
      setError('Không thể upload hình ảnh');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (category, filename) => {
    if (!window.confirm('Bạn có chắc muốn xóa hình ảnh này?')) return;

    try {
      await imageAPI.deleteImage(category, filename);
      setSuccess('Xóa hình ảnh thành công');
      loadImages();
    } catch (error) {
      console.error('Error deleting image:', error);
      setError('Không thể xóa hình ảnh');
    }
  };

  const handleBulkDelete = async () => {
    if (selectedImages.length === 0) return;
    if (!window.confirm(`Bạn có chắc muốn xóa ${selectedImages.length} hình ảnh?`)) return;

    try {
      await imageAPI.bulkDeleteImages(selectedImages);
      setSuccess(`Xóa thành công ${selectedImages.length} hình ảnh`);
      setSelectedImages([]);
      loadImages();
    } catch (error) {
      console.error('Error bulk deleting images:', error);
      setError('Không thể xóa hình ảnh');
    }
  };

  const handleSearch = async () => {
    try {
      setLoading(true);
      const response = await imageAPI.searchImages(searchQuery, selectedCategory, 0, 20);
      const data = response.data;
      
      setImages(data.images || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error('Error searching images:', error);
      setError('Không thể tìm kiếm hình ảnh');
    } finally {
      setLoading(false);
    }
  };

  const handleImageSelect = (image) => {
    if (onImageSelect) {
      onImageSelect(image);
      onClose();
    }
  };

  const toggleImageSelection = (image) => {
    setSelectedImages(prev => {
      const exists = prev.find(img => img.category === image.category && img.filename === image.filename);
      if (exists) {
        return prev.filter(img => !(img.category === image.category && img.filename === image.filename));
      } else {
        return [...prev, image];
      }
    });
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container image-management-modal">
        <div className="modal-header">
          <h2 className="modal-title">
            <ImageIcon className="modal-title-icon" />
            Quản lý hình ảnh
          </h2>
          <button className="modal-close" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-content">
          {/* Upload Section */}
          <div className="upload-section">
            <h3>Upload hình ảnh</h3>
            <div className="upload-controls">
              <select 
                value={selectedCategory} 
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="form-input"
              >
                <option value="">Chọn danh mục</option>
                {categories.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
              <input
                type="file"
                multiple
                accept="image/*"
                onChange={(e) => handleUpload(e.target.files, selectedCategory)}
                className="form-input"
                disabled={!selectedCategory || uploading}
              />
              {uploading && <span>Đang upload...</span>}
            </div>
            {uploadInfo && (
              <div className="upload-info">
                <small>
                  Tối đa {uploadInfo.maxFileSize}, hỗ trợ: {uploadInfo.allowedExtensions?.join(', ')}
                </small>
              </div>
            )}
          </div>

          {/* Search and Filter */}
          <div className="search-section">
            <div className="search-controls">
              <input
                type="text"
                placeholder="Tìm kiếm hình ảnh..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="form-input"
              />
              <button onClick={handleSearch} className="btn btn-primary">
                <Search size={16} />
                Tìm kiếm
              </button>
            </div>
            <div className="filter-controls">
              <select 
                value={selectedCategory} 
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="form-input"
              >
                <option value="">Tất cả danh mục</option>
                {categories.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Error/Success Messages */}
          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}
          {success && (
            <div className="alert alert-success">
              {success}
            </div>
          )}

          {/* Bulk Actions */}
          {selectedImages.length > 0 && (
            <div className="bulk-actions">
              <span>{selectedImages.length} hình ảnh được chọn</span>
              <button onClick={handleBulkDelete} className="btn btn-danger">
                <Trash2 size={16} />
                Xóa đã chọn
              </button>
            </div>
          )}

          {/* Images Grid */}
          <div className="images-grid">
            {loading ? (
              <div className="loading-state">
                <div className="loading-spinner"></div>
                <p>Đang tải hình ảnh...</p>
              </div>
            ) : images.length === 0 ? (
              <div className="empty-state">
                <ImageIcon size={48} />
                <p>Không có hình ảnh nào</p>
              </div>
            ) : (
              images.map((image, index) => (
                <div key={`${image.category}-${image.filename}-${index}`} className="image-card">
                  <div className="image-preview">
                    <img 
                      src={`/uploads/${image.category}/${image.filename}`} 
                      alt={image.filename}
                      onError={(e) => {
                        e.target.src = '/placeholder-image.png';
                      }}
                    />
                    <div className="image-overlay">
                      <button 
                        onClick={() => handleImageSelect(image)}
                        className="btn btn-sm btn-primary"
                        title="Chọn hình ảnh"
                      >
                        <Eye size={14} />
                      </button>
                      <button 
                        onClick={() => handleDelete(image.category, image.filename)}
                        className="btn btn-sm btn-danger"
                        title="Xóa"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                    <input
                      type="checkbox"
                      checked={selectedImages.some(img => img.category === image.category && img.filename === image.filename)}
                      onChange={() => toggleImageSelection(image)}
                      className="image-checkbox"
                    />
                  </div>
                  <div className="image-info">
                    <div className="image-name" title={image.filename}>
                      {image.filename}
                    </div>
                    <div className="image-category">{image.category}</div>
                    {image.size && (
                      <div className="image-size">
                        {(image.size / 1024).toFixed(1)} KB
                      </div>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button 
                onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                disabled={currentPage === 0}
                className="btn btn-secondary"
              >
                Trước
              </button>
              <span>Trang {currentPage + 1} / {totalPages}</span>
              <button 
                onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                disabled={currentPage >= totalPages - 1}
                className="btn btn-secondary"
              >
                Sau
              </button>
            </div>
          )}
        </div>

        <div className="modal-actions">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
};

export default ImageManagementModal;