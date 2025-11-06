import React, { useState, useEffect } from 'react';
import { X, DollarSign, CreditCard, Save } from 'lucide-react';
import { dealerPaymentAPI, installmentPlanAPI } from '../../services/api';
import { useAuth } from '../../contexts/AuthContext';
import toast from 'react-hot-toast';
import './Modal.css';

const DealerPaymentModal = ({ invoice, isOpen, onClose, onSave, mode = 'create' }) => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    invoiceId: '',
    amount: '',
    paymentType: 'BANK_TRANSFER',
    referenceNumber: '',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [isInstallment, setIsInstallment] = useState(false);
  const [installmentPlan, setInstallmentPlan] = useState({
    totalAmount: '',
    downPaymentAmount: '',
    loanAmount: '',
    interestRate: 8.5,
    loanTermMonths: 12,
    monthlyPaymentAmount: '',
    firstPaymentDate: '',
    financeCompany: ''
  });

  useEffect(() => {
    if (isOpen && invoice) {
      setFormData({
        invoiceId: invoice.invoiceId || '',
        amount: invoice.totalAmount || invoice.remainingAmount || '',
        paymentType: 'BANK_TRANSFER',
        referenceNumber: '',
        notes: ''
      });
      
      setInstallmentPlan({
        totalAmount: invoice.totalAmount || '',
        downPaymentAmount: (invoice.totalAmount * 0.3) || '',
        loanAmount: '',
        interestRate: 8.5,
        loanTermMonths: 12,
        monthlyPaymentAmount: '',
        firstPaymentDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        financeCompany: ''
      });
    }
  }, [isOpen, invoice]);

  const calculateInstallment = () => {
    const total = parseFloat(installmentPlan.totalAmount) || 0;
    const downPayment = parseFloat(installmentPlan.downPaymentAmount) || 0;
    const loanAmount = total - downPayment;
    const monthlyRate = (parseFloat(installmentPlan.interestRate) || 0) / 100 / 12;
    const term = parseInt(installmentPlan.loanTermMonths) || 12;
    
    if (loanAmount > 0 && monthlyRate > 0 && term > 0) {
      const monthlyPayment = (loanAmount * monthlyRate * Math.pow(1 + monthlyRate, term)) 
        / (Math.pow(1 + monthlyRate, term) - 1);
      
      setInstallmentPlan(prev => ({
        ...prev,
        loanAmount: loanAmount,
        monthlyPaymentAmount: monthlyPayment.toFixed(0)
      }));
    }
  };

  useEffect(() => {
    if (isInstallment && installmentPlan.totalAmount && installmentPlan.downPaymentAmount) {
      calculateInstallment();
    }
  }, [installmentPlan.totalAmount, installmentPlan.downPaymentAmount, installmentPlan.interestRate, installmentPlan.loanTermMonths, isInstallment]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleInstallmentChange = (e) => {
    const { name, value } = e.target;
    setInstallmentPlan(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (isInstallment) {
      // Create installment plan first
      if (!installmentPlan.totalAmount || !installmentPlan.downPaymentAmount || !installmentPlan.loanTermMonths) {
        toast.error('Vui lòng điền đầy đủ thông tin trả góp');
        return;
      }

      try {
        setLoading(true);
        // Theo guide line 1477-1496: Phải dùng POST /api/installment-plans với planType="dealer"
        // Không dùng /api/dealer-installment-plans
        const dealerId = invoice?.dealerId || invoice?.dealer?.dealerId || user?.dealerId;
        if (!dealerId) {
          toast.error('Không tìm thấy thông tin đại lý. Vui lòng kiểm tra lại.');
          return;
        }
        
        const planData = {
          planType: 'dealer', // Required: "dealer" hoặc "customer" (String)
          invoiceId: formData.invoiceId, // Required cho dealer plan
          dealerId: dealerId, // Required cho dealer plan
          totalAmount: parseFloat(installmentPlan.totalAmount),
          downPaymentAmount: parseFloat(installmentPlan.downPaymentAmount),
          loanAmount: parseFloat(installmentPlan.loanAmount),
          interestRate: parseFloat(installmentPlan.interestRate || 0.02),
          loanTermMonths: parseInt(installmentPlan.loanTermMonths),
          monthlyPaymentAmount: parseFloat(installmentPlan.monthlyPaymentAmount),
          ...(installmentPlan.firstPaymentDate ? { firstPaymentDate: installmentPlan.firstPaymentDate } : {}),
          ...(installmentPlan.financeCompany ? { financeCompany: installmentPlan.financeCompany } : {}),
          ...(installmentPlan.contractNumber ? { contractNumber: installmentPlan.contractNumber } : {})
        };

        await installmentPlanAPI.createInstallmentPlan(planData);
        toast.success('Đã tạo kế hoạch trả góp thành công');

        // Process down payment
        // Required fields
        const paymentData = {
          invoiceId: formData.invoiceId,
          amount: parseFloat(installmentPlan.downPaymentAmount),
          paymentType: 'INSTALLMENT'
        };
        
        // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
        if (formData.referenceNumber?.trim()) {
          paymentData.referenceNumber = formData.referenceNumber.trim();
        } else {
          paymentData.referenceNumber = `TXN-DP-${Date.now()}`;
        }
        if (formData.notes?.trim()) {
          paymentData.notes = formData.notes.trim();
        } else {
          paymentData.notes = 'Thanh toán đợt đầu';
        }

        await dealerPaymentAPI.processPayment(paymentData);
        toast.success('Đã thanh toán đợt đầu thành công');
        
        onSave && onSave();
        onClose();
      } catch (error) {
        console.error('Error processing installment payment:', error);
        toast.error(error.response?.data?.error || 'Không thể xử lý thanh toán trả góp');
      } finally {
        setLoading(false);
      }
    } else {
      // Full payment
      if (!formData.invoiceId || !formData.amount) {
        toast.error('Vui lòng điền đầy đủ thông tin');
        return;
      }

      try {
        setLoading(true);
        // Required fields (theo FIELD_REFERENCE_GUIDE.md line 499-508)
        const paymentData = {
          invoiceId: formData.invoiceId,
          amount: parseFloat(formData.amount)
        };
        
        // Optional fields - chỉ thêm nếu có giá trị (không gửi null/undefined/empty)
        if (formData.paymentType) {
          paymentData.paymentType = formData.paymentType;
        }
        if (formData.referenceNumber?.trim()) {
          paymentData.referenceNumber = formData.referenceNumber.trim();
        } else {
          // Auto-generate reference number nếu không có
          paymentData.referenceNumber = `TXN-${Date.now()}`;
        }
        if (formData.notes?.trim()) {
          paymentData.notes = formData.notes.trim();
        }

        await dealerPaymentAPI.processPayment(paymentData);
        toast.success('Thanh toán thành công');
        
        onSave && onSave();
        onClose();
      } catch (error) {
        console.error('Error processing payment:', error);
        toast.error(error.response?.data?.error || 'Không thể xử lý thanh toán');
      } finally {
        setLoading(false);
      }
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-container">
        <div className="modal-header">
          <div className="modal-title">
            <DollarSign size={24} />
            <h2>Thanh toán Invoice</h2>
          </div>
          <button className="modal-close" onClick={onClose}>
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>
              <input
                type="radio"
                checked={!isInstallment}
                onChange={() => setIsInstallment(false)}
              />
              <span>Trả đủ</span>
            </label>
            <label style={{ marginLeft: '20px' }}>
              <input
                type="radio"
                checked={isInstallment}
                onChange={() => setIsInstallment(true)}
              />
              <span>Trả góp</span>
            </label>
          </div>

          {!isInstallment ? (
            <>
              <div className="form-group">
                <label htmlFor="invoiceId">Invoice ID *</label>
                <input
                  id="invoiceId"
                  name="invoiceId"
                  type="text"
                  className="form-input"
                  value={formData.invoiceId}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="amount">Số tiền (VNĐ) *</label>
                <input
                  id="amount"
                  name="amount"
                  type="number"
                  min="0"
                  step="1000"
                  className="form-input"
                  value={formData.amount}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="paymentType">Loại thanh toán</label>
                <select
                  id="paymentType"
                  name="paymentType"
                  className="form-select"
                  value={formData.paymentType}
                  onChange={handleInputChange}
                >
                  <option value="BANK_TRANSFER">Chuyển khoản</option>
                  <option value="CASH">Tiền mặt</option>
                  <option value="CREDIT_CARD">Thẻ tín dụng</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="referenceNumber">Số tham chiếu</label>
                <input
                  id="referenceNumber"
                  name="referenceNumber"
                  type="text"
                  className="form-input"
                  value={formData.referenceNumber}
                  onChange={handleInputChange}
                  placeholder="TXN-..."
                />
              </div>

              <div className="form-group">
                <label htmlFor="notes">Ghi chú</label>
                <textarea
                  id="notes"
                  name="notes"
                  className="form-input"
                  rows={3}
                  value={formData.notes}
                  onChange={handleInputChange}
                  placeholder="Ghi chú về thanh toán..."
                />
              </div>
            </>
          ) : (
            <>
              <div className="form-group">
                <label htmlFor="installmentInvoiceId">Invoice ID *</label>
                <input
                  id="installmentInvoiceId"
                  name="invoiceId"
                  type="text"
                  className="form-input"
                  value={formData.invoiceId}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="totalAmount">Tổng tiền (VNĐ) *</label>
                <input
                  id="totalAmount"
                  name="totalAmount"
                  type="number"
                  min="0"
                  step="1000"
                  className="form-input"
                  value={installmentPlan.totalAmount}
                  onChange={handleInstallmentChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="downPaymentAmount">Số tiền trả trước (VNĐ) *</label>
                <input
                  id="downPaymentAmount"
                  name="downPaymentAmount"
                  type="number"
                  min={installmentPlan.totalAmount * 0.1}
                  max={installmentPlan.totalAmount * 0.9}
                  step="1000"
                  className="form-input"
                  value={installmentPlan.downPaymentAmount}
                  onChange={handleInstallmentChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="loanTermMonths">Kỳ hạn (tháng) *</label>
                <select
                  id="loanTermMonths"
                  name="loanTermMonths"
                  className="form-select"
                  value={installmentPlan.loanTermMonths}
                  onChange={handleInstallmentChange}
                  required
                >
                  <option value={6}>6 tháng</option>
                  <option value={12}>12 tháng</option>
                  <option value={24}>24 tháng</option>
                  <option value={36}>36 tháng</option>
                </select>
              </div>

              <div className="form-group">
                <label htmlFor="interestRate">Lãi suất (%/năm)</label>
                <input
                  id="interestRate"
                  name="interestRate"
                  type="number"
                  min="0"
                  max="30"
                  step="0.1"
                  className="form-input"
                  value={installmentPlan.interestRate}
                  onChange={handleInstallmentChange}
                />
              </div>

              {installmentPlan.monthlyPaymentAmount && (
                <div className="form-group">
                  <label>Số tiền trả hàng tháng:</label>
                  <div className="amount-display">
                    {Number(installmentPlan.monthlyPaymentAmount).toLocaleString('vi-VN')} VNĐ
                  </div>
                </div>
              )}

              <div className="form-group">
                <label htmlFor="firstPaymentDate">Ngày thanh toán đầu tiên</label>
                <input
                  id="firstPaymentDate"
                  name="firstPaymentDate"
                  type="date"
                  className="form-input"
                  value={installmentPlan.firstPaymentDate}
                  onChange={handleInstallmentChange}
                />
              </div>

              <div className="form-group">
                <label htmlFor="financeCompany">Công ty tài chính</label>
                <input
                  id="financeCompany"
                  name="financeCompany"
                  type="text"
                  className="form-input"
                  value={installmentPlan.financeCompany}
                  onChange={handleInstallmentChange}
                  placeholder="Tên công ty tài chính..."
                />
              </div>

              <div className="form-group">
                <label htmlFor="referenceNumber">Số tham chiếu</label>
                <input
                  id="referenceNumber"
                  name="referenceNumber"
                  type="text"
                  className="form-input"
                  value={formData.referenceNumber}
                  onChange={handleInputChange}
                  placeholder="TXN-DP-..."
                />
              </div>

              <div className="form-group">
                <label htmlFor="notes">Ghi chú</label>
                <textarea
                  id="notes"
                  name="notes"
                  className="form-input"
                  rows={3}
                  value={formData.notes}
                  onChange={handleInputChange}
                  placeholder="Ghi chú về thanh toán..."
                />
              </div>
            </>
          )}

          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={onClose}>
              Hủy
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              <Save size={20} />
              {loading ? 'Đang xử lý...' : isInstallment ? 'Xác nhận Trả Góp' : 'Thanh toán'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DealerPaymentModal;

