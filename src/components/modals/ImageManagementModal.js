import React, { useState, useEffect } from 'react';
import { X, Upload, Search, Filter, Eye, Edit, Trash2, Move, Download } from 'lucide-react';
import { imageAPI } from '../../services/api';
import './Modal.css';

const ImageManagementModal = ({ isOpen, onClose, mode, image, onSave }) => {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedImages, setSelectedImages] = useState([]);
  const [categories, setCategories] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    if (isOpen) {
      loadImages();
      loadCategories();
    }
  }, [isOpen, currentPage, searchTerm, selectedCategory]);

  const loadImages = async () => {
    try {
      setLoading(true);
      const response = await imageAPI.getImageList(currentPage, 20);
      setImages(response.data?.content || []);
      setTotalPages(response.data?.totalPages || 0);
    } catch (error) {
      console.error('Error loading images:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadCategories = async () => {
    try {
      const response = await imageAPI.getImageStats();
      setCategories(response.data?.categoryStats || []);
    } catch (error) {
      console.error('Error loading categories:', error);
    }
  };

  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(0);
  };

  const handleCategoryFilter = (category) => {
    setSelectedCategory(category);
    setCurrentPage(0);
  };

  const handleImageSelect = (imageId) => {
    setSelectedImages(prev => 
      prev.includes(imageId) 
        ? prev.filter(id => id !== imageId)
        : [...prev, imageId]
    );
  };

  const handleSelectAll = () => {
    if (selectedImages.length === images.length) {
      setSelectedImages([]);
    } else {
      setSelectedImages(images.map(img => img.id));
    }
  };

  const handleBulkDelete = async () => {
    if (selectedImages.length === 0) return;
    
    if (window.confirm(`Bạn có chắc chắn muốn xóa ${selectedImages.length} hình ảnh?`)) {
      try {
        await imageAPI.bulkDeleteImages(selectedImages);
        setSelectedImages([]);
        loadImages();
        onSave();
      } catch (error) {
        console.error('Error deleting images:', error);
      }
    }
  };

  const handleBulkMove = async () => {
    if (selectedImages.length === 0) return;
    
    const newCategory = prompt('Nhập danh mục mới:');
    if (newCategory) {
      try {
        await imageAPI.bulkMoveImages(selectedImages, newCategory);
        setSelectedImages([]);
        loadImages();
        onSave();
      } catch (error) {
        console.error('Error moving images:', error);
      }
    }
  };

  const handleDeleteImage = async (image) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa hình ảnh này?')) {
      try {
        await imageAPI.deleteImage(image.category, image.filename);
        loadImages();
        onSave();
      } catch (error) {
        console.error('Error deleting image:', error);
      }
    }
  };

  const handleRenameImage = async (image) => {
    const newName = prompt('Nhập tên mới:', image.filename);
    if (newName && newName !== image.filename) {
      try {
        await imageAPI.renameImage(image.category, image.filename, newName);
        loadImages();
        onSave();
      } catch (error) {
        console.error('Error renaming image:', error);
      }
    }
  };

  const handleMoveImage = async (image) => {
    const newCategory = prompt('Nhập danh mục mới:', image.category);
    if (newCategory && newCategory !== image.category) {
      try {
        await imageAPI.moveImage(image.category, image.filename, newCategory);
        loadImages();
        onSave();
      } catch (error) {
        console.error('Error moving image:', error);
      }
    }
  };

  const getImageUrl = (image) => {
    return `http://localhost:8080/uploads/${image.category}/${image.filename}`;
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content large">
        <div className="modal-header">
          <h2>Quản lý hình ảnh</h2>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-body">
          {/* Search and Filter */}
          <div className="search-controls">
            <div className="search-bar">
              <Search className="search-icon" />
              <input
                type="text"
                placeholder="Tìm kiếm hình ảnh..."
                value={searchTerm}
                onChange={handleSearch}
                className="search-input"
              />
            </div>
            <select
              value={selectedCategory}
              onChange={(e) => handleCategoryFilter(e.target.value)}
              className="filter-select"
            >
              <option value="">Tất cả danh mục</option>
              {categories.map(cat => (
                <option key={cat.category} value={cat.category}>
                  {cat.category} ({cat.count})
                </option>
              ))}
            </select>
          </div>

          {/* Bulk Actions */}
          {selectedImages.length > 0 && (
            <div className="bulk-actions">
              <span>{selectedImages.length} hình ảnh đã chọn</span>
              <button onClick={handleBulkMove}>Di chuyển</button>
              <button onClick={handleBulkDelete} className="danger">Xóa</button>
              <button onClick={() => setSelectedImages([])}>Bỏ chọn</button>
            </div>
          )}

          {/* Images Grid */}
          <div className="images-grid">
            {loading ? (
              <div className="loading">Đang tải...</div>
            ) : images.length === 0 ? (
              <div className="no-data">Không có hình ảnh nào</div>
            ) : (
              <>
                <div className="select-all">
                  <label>
                    <input
                      type="checkbox"
                      checked={selectedImages.length === images.length && images.length > 0}
                      onChange={handleSelectAll}
                    />
                    Chọn tất cả
                  </label>
                </div>
                {images.map(image => (
                  <div key={image.id} className="image-card">
                    <div className="image-checkbox">
                      <input
                        type="checkbox"
                        checked={selectedImages.includes(image.id)}
                        onChange={() => handleImageSelect(image.id)}
                      />
                    </div>
                    <div className="image-preview">
                      <img
                        src={getImageUrl(image)}
                        alt={image.filename}
                        onError={(e) => {
                          e.target.style.display = 'none';
                          e.target.nextSibling.style.display = 'flex';
                        }}
                      />
                      <div className="image-placeholder" style={{ display: 'none' }}>
                        <Eye size={24} />
                      </div>
                    </div>
                    <div className="image-info">
                      <div className="image-filename" title={image.filename}>
                        {image.filename}
                      </div>
                      <div className="image-meta">
                        {image.category} • {formatFileSize(image.fileSize)}
                      </div>
                      <div className="image-date">
                        {new Date(image.uploadDate).toLocaleDateString('vi-VN')}
                      </div>
                    </div>
                    <div className="image-actions">
                      <button
                        className="btn-icon"
                        onClick={() => window.open(getImageUrl(image), '_blank')}
                        title="Xem"
                      >
                        <Eye size={16} />
                      </button>
                      <button
                        className="btn-icon"
                        onClick={() => handleRenameImage(image)}
                        title="Đổi tên"
                      >
                        <Edit size={16} />
                      </button>
                      <button
                        className="btn-icon"
                        onClick={() => handleMoveImage(image)}
                        title="Di chuyển"
                      >
                        <Move size={16} />
                      </button>
                      <button
                        className="btn-icon danger"
                        onClick={() => handleDeleteImage(image)}
                        title="Xóa"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </div>
                ))}
              </>
            )}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination">
              <button
                onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                disabled={currentPage === 0}
              >
                Trước
              </button>
              <span>
                Trang {currentPage + 1} / {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                disabled={currentPage === totalPages - 1}
              >
                Sau
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ImageManagementModal;


