--
-- PostgreSQL database dump
--

\restrict lZCilG6Hfx4j7dEPpRmT3S2SwE6KHG0JOXHYvMTJspDbkby6b4lDwTT1VxoOgSQ

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: update_dealer_order_items_updated_at(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_dealer_order_items_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_dealer_order_items_updated_at() OWNER TO postgres;

--
-- Name: update_dealer_quotation_expiry_date(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_dealer_quotation_expiry_date() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.quotation_date IS NOT NULL AND NEW.validity_days IS NOT NULL THEN
        NEW.expiry_date := NEW.quotation_date + (NEW.validity_days || ' days')::INTERVAL;
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_dealer_quotation_expiry_date() OWNER TO postgres;

--
-- Name: update_dealer_quotation_updated_at(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_dealer_quotation_updated_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at := CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_dealer_quotation_updated_at() OWNER TO postgres;

--
-- Name: update_updated_at_column(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_updated_at_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_updated_at_column() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: appointments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.appointments (
    appointment_id uuid DEFAULT gen_random_uuid() NOT NULL,
    customer_id uuid,
    staff_id uuid,
    appointment_type character varying(50) DEFAULT 'consultation'::character varying,
    title character varying(255) NOT NULL,
    description text,
    appointment_date timestamp without time zone NOT NULL,
    duration_minutes integer DEFAULT 60,
    location character varying(255),
    status character varying(50) DEFAULT 'scheduled'::character varying,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    variant_id integer,
    CONSTRAINT appointments_appointment_type_check CHECK (((appointment_type)::text = ANY (ARRAY[('consultation'::character varying)::text, ('test_drive'::character varying)::text, ('delivery'::character varying)::text, ('service'::character varying)::text, ('maintenance'::character varying)::text]))),
    CONSTRAINT appointments_status_check CHECK (((status)::text = ANY (ARRAY[('scheduled'::character varying)::text, ('confirmed'::character varying)::text, ('completed'::character varying)::text, ('cancelled'::character varying)::text])))
);


ALTER TABLE public.appointments OWNER TO postgres;

--
-- Name: customer_feedbacks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.customer_feedbacks (
    feedback_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    customer_id uuid,
    order_id uuid,
    rating integer,
    feedback_type character varying(50) DEFAULT 'general'::character varying,
    message text NOT NULL,
    response text,
    status character varying(50) DEFAULT 'open'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT customer_feedbacks_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);


ALTER TABLE public.customer_feedbacks OWNER TO postgres;

--
-- Name: customer_invoices; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.customer_invoices (
    invoice_id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    discount_amount numeric(15,2),
    due_date date NOT NULL,
    invoice_date date NOT NULL,
    invoice_number character varying(100) NOT NULL,
    notes text,
    payment_terms_days integer,
    quotation_id uuid,
    status character varying(50) NOT NULL,
    subtotal numeric(15,2) NOT NULL,
    tax_amount numeric(15,2),
    total_amount numeric(15,2) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    customer_id uuid,
    evm_staff_id uuid,
    order_id uuid,
    CONSTRAINT customer_invoices_date_range_check CHECK ((due_date >= invoice_date)),
    CONSTRAINT customer_invoices_status_check CHECK (((status)::text = ANY ((ARRAY['ISSUED'::character varying, 'PARTIALLY_PAID'::character varying, 'PAID'::character varying, 'OVERDUE'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT customer_invoices_total_amount_check CHECK ((total_amount >= (0)::numeric))
);


ALTER TABLE public.customer_invoices OWNER TO postgres;

--
-- Name: customer_payments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.customer_payments (
    payment_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_id uuid,
    customer_id uuid,
    payment_number character varying(100) NOT NULL,
    payment_date date NOT NULL,
    amount numeric(15,2) NOT NULL,
    payment_type character varying(50),
    payment_method character varying(100),
    reference_number character varying(100),
    status character varying(50) DEFAULT 'pending'::character varying,
    processed_by uuid,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.customer_payments OWNER TO postgres;

--
-- Name: customers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.customers (
    customer_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    email character varying(255),
    phone character varying(20),
    date_of_birth date,
    address text,
    city character varying(100),
    province character varying(100),
    postal_code character varying(20),
    credit_score integer,
    preferred_contact_method character varying(50),
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.customers OWNER TO postgres;

--
-- Name: dealer_contracts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_contracts (
    contract_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    contract_number character varying(100) NOT NULL,
    contract_type character varying(50) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    territory character varying(255),
    commission_rate numeric(5,2),
    minimum_sales_target numeric(15,2),
    contract_status character varying(50) DEFAULT 'active'::character varying,
    signed_date date,
    contract_file_url character varying(500),
    contract_file_path character varying(500),
    terms_and_conditions text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    dealer_id uuid,
    monthly_target numeric(15,2),
    yearly_target numeric(15,2),
    CONSTRAINT dealer_contracts_contract_status_check CHECK (((contract_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'EXPIRED'::character varying, 'TERMINATED'::character varying, 'SUSPENDED'::character varying])::text[]))),
    CONSTRAINT dealer_contracts_date_range_check CHECK ((end_date >= start_date))
);


ALTER TABLE public.dealer_contracts OWNER TO postgres;

--
-- Name: dealer_discount_policies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_discount_policies (
    policy_id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    discount_amount numeric(15,2),
    discount_percent numeric(5,2),
    end_date date NOT NULL,
    policy_name character varying(255) NOT NULL,
    start_date date NOT NULL,
    status character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    variant_id integer NOT NULL
);


ALTER TABLE public.dealer_discount_policies OWNER TO postgres;

--
-- Name: dealer_installment_plans; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_installment_plans (
    plan_id uuid NOT NULL,
    contract_number character varying(100),
    created_at timestamp(6) without time zone NOT NULL,
    down_payment_amount numeric(15,2) NOT NULL,
    finance_company character varying(255),
    first_payment_date date,
    interest_rate numeric(5,2) NOT NULL,
    last_payment_date date,
    loan_amount numeric(15,2) NOT NULL,
    loan_term_months integer NOT NULL,
    monthly_payment_amount numeric(15,2) NOT NULL,
    plan_status character varying(50) NOT NULL,
    total_amount numeric(15,2) NOT NULL,
    invoice_id uuid
);


ALTER TABLE public.dealer_installment_plans OWNER TO postgres;

--
-- Name: dealer_installment_schedules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_installment_schedules (
    schedule_id uuid NOT NULL,
    amount numeric(15,2) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    due_date date NOT NULL,
    installment_number integer NOT NULL,
    interest_amount numeric(15,2),
    late_fee numeric(15,2),
    notes text,
    paid_amount numeric(15,2),
    paid_date date,
    principal_amount numeric(15,2),
    status character varying(50) NOT NULL,
    plan_id uuid NOT NULL
);


ALTER TABLE public.dealer_installment_schedules OWNER TO postgres;

--
-- Name: dealer_invoices; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_invoices (
    invoice_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    invoice_number character varying(100) NOT NULL,
    dealer_order_id uuid,
    evm_staff_id uuid,
    invoice_date date NOT NULL,
    due_date date NOT NULL,
    subtotal numeric(15,2) NOT NULL,
    tax_amount numeric(15,2) DEFAULT 0,
    discount_amount numeric(15,2) DEFAULT 0,
    total_amount numeric(15,2) NOT NULL,
    status character varying(50) DEFAULT 'issued'::character varying,
    payment_terms_days integer DEFAULT 30,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    quotation_id uuid,
    CONSTRAINT dealer_invoices_date_range_check CHECK ((due_date >= invoice_date)),
    CONSTRAINT dealer_invoices_total_amount_check CHECK ((total_amount >= (0)::numeric))
);


ALTER TABLE public.dealer_invoices OWNER TO postgres;

--
-- Name: COLUMN dealer_invoices.quotation_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dealer_invoices.quotation_id IS 'Reference to DealerQuotation if invoice was created from quotation';


--
-- Name: dealer_order_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_order_items (
    item_id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    discount_amount numeric(15,2),
    discount_percentage numeric(5,2),
    final_price numeric(15,2) NOT NULL,
    notes text,
    quantity integer NOT NULL,
    status character varying(50) NOT NULL,
    total_price numeric(15,2) NOT NULL,
    unit_price numeric(15,2) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    color_id integer NOT NULL,
    dealer_order_id uuid NOT NULL,
    variant_id integer NOT NULL,
    CONSTRAINT dealer_order_items_discount_percentage_check CHECK (((discount_percentage IS NULL) OR ((discount_percentage >= (0)::numeric) AND (discount_percentage <= (100)::numeric)))),
    CONSTRAINT dealer_order_items_quantity_check CHECK ((quantity > 0))
);


ALTER TABLE public.dealer_order_items OWNER TO postgres;

--
-- Name: dealer_orders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_orders (
    dealer_order_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    dealer_order_number character varying(100) NOT NULL,
    evm_staff_id uuid,
    order_date date NOT NULL,
    expected_delivery_date date,
    total_quantity integer NOT NULL,
    total_amount numeric(15,2) NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    priority character varying(20) DEFAULT 'normal'::character varying,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    approved_at timestamp(6) without time zone,
    approved_by uuid,
    rejection_reason text,
    dealer_id uuid,
    order_type character varying(50) DEFAULT 'PURCHASE'::character varying,
    approval_status character varying(50) DEFAULT 'PENDING'::character varying,
    payment_terms character varying(50) DEFAULT 'NET_30'::character varying,
    delivery_terms character varying(50) DEFAULT 'FOB_FACTORY'::character varying,
    discount_applied numeric(5,2) DEFAULT 0.00,
    discount_reason text,
    CONSTRAINT dealer_orders_approval_status_check CHECK (((approval_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT dealer_orders_order_type_check CHECK (((order_type)::text = ANY ((ARRAY['PURCHASE'::character varying, 'RESERVE'::character varying, 'SAMPLE'::character varying])::text[]))),
    CONSTRAINT dealer_orders_status_check CHECK (((status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('APPROVED'::character varying)::text, ('REJECTED'::character varying)::text, ('CONFIRMED'::character varying)::text, ('WAITING_FOR_QUOTATION'::character varying)::text, ('IN_PRODUCTION'::character varying)::text, ('READY_FOR_DELIVERY'::character varying)::text, ('DELIVERED'::character varying)::text, ('CANCELLED'::character varying)::text]))),
    CONSTRAINT dealer_orders_total_amount_check CHECK ((total_amount >= (0)::numeric))
);


ALTER TABLE public.dealer_orders OWNER TO postgres;

--
-- Name: dealer_payments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_payments (
    payment_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    invoice_id uuid,
    payment_number character varying(100) NOT NULL,
    payment_date date NOT NULL,
    amount numeric(15,2) NOT NULL,
    payment_type character varying(50),
    reference_number character varying(100),
    status character varying(50) DEFAULT 'pending'::character varying,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.dealer_payments OWNER TO postgres;

--
-- Name: dealer_quotation_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_quotation_items (
    item_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    quotation_id uuid NOT NULL,
    variant_id integer NOT NULL,
    color_id integer NOT NULL,
    quantity integer NOT NULL,
    unit_price numeric(15,2) NOT NULL,
    discount_percentage numeric(5,2) DEFAULT 0,
    discount_amount numeric(15,2) DEFAULT 0,
    total_price numeric(15,2) NOT NULL,
    notes text,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT dealer_quotation_items_discount_percentage_check CHECK (((discount_percentage IS NULL) OR ((discount_percentage >= (0)::numeric) AND (discount_percentage <= (100)::numeric)))),
    CONSTRAINT dealer_quotation_items_quantity_check CHECK ((quantity > 0)),
    CONSTRAINT dealer_quotation_items_total_price_check CHECK ((total_price >= (0)::numeric)),
    CONSTRAINT dealer_quotation_items_unit_price_check CHECK ((unit_price >= (0)::numeric))
);


ALTER TABLE public.dealer_quotation_items OWNER TO postgres;

--
-- Name: TABLE dealer_quotation_items; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.dealer_quotation_items IS 'Chi tiết từng sản phẩm trong báo giá đại lý';


--
-- Name: dealer_quotations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_quotations (
    quotation_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    quotation_number character varying(100) NOT NULL,
    dealer_id uuid NOT NULL,
    dealer_order_id uuid,
    evm_staff_id uuid,
    quotation_date date NOT NULL,
    validity_days integer DEFAULT 30 NOT NULL,
    expiry_date date,
    subtotal numeric(15,2) NOT NULL,
    tax_amount numeric(15,2) DEFAULT 0,
    discount_amount numeric(15,2) DEFAULT 0,
    discount_percentage numeric(5,2) DEFAULT 0,
    total_amount numeric(15,2) NOT NULL,
    status character varying(50) DEFAULT 'pending'::character varying NOT NULL,
    payment_terms character varying(100),
    delivery_terms character varying(255),
    expected_delivery_date date,
    accepted_at timestamp(6) without time zone,
    rejected_at timestamp(6) without time zone,
    rejection_reason text,
    notes text,
    created_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp(6) without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT dealer_quotations_discount_percentage_check CHECK (((discount_percentage IS NULL) OR ((discount_percentage >= (0)::numeric) AND (discount_percentage <= (100)::numeric)))),
    CONSTRAINT dealer_quotations_status_check CHECK (((status)::text = ANY ((ARRAY['pending'::character varying, 'sent'::character varying, 'accepted'::character varying, 'rejected'::character varying, 'expired'::character varying, 'converted'::character varying])::text[])))
);


ALTER TABLE public.dealer_quotations OWNER TO postgres;

--
-- Name: TABLE dealer_quotations; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.dealer_quotations IS 'Báo giá từ hãng cho đại lý khi đại lý đặt xe';


--
-- Name: COLUMN dealer_quotations.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.dealer_quotations.status IS 'pending, sent, accepted, rejected, expired, converted';


--
-- Name: dealer_targets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealer_targets (
    target_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    target_year integer NOT NULL,
    target_month integer,
    target_type character varying(50) NOT NULL,
    target_amount numeric(15,2) NOT NULL,
    target_quantity integer,
    achieved_amount numeric(15,2) DEFAULT 0,
    achieved_quantity integer DEFAULT 0,
    achievement_rate numeric(5,2) GENERATED ALWAYS AS (
CASE
    WHEN (target_amount > (0)::numeric) THEN ((achieved_amount / target_amount) * (100)::numeric)
    ELSE (0)::numeric
END) STORED,
    target_status character varying(50) DEFAULT 'active'::character varying,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    dealer_id uuid,
    target_scope character varying(50) DEFAULT 'dealer'::character varying
);


ALTER TABLE public.dealer_targets OWNER TO postgres;

--
-- Name: dealers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dealers (
    dealer_id uuid DEFAULT gen_random_uuid() NOT NULL,
    dealer_code character varying(50) NOT NULL,
    dealer_name character varying(255) NOT NULL,
    contact_person character varying(255),
    email character varying(255),
    phone character varying(20),
    address text,
    city character varying(100),
    province character varying(100),
    postal_code character varying(20),
    dealer_type character varying(50) DEFAULT 'authorized'::character varying,
    license_number character varying(100),
    tax_code character varying(50),
    bank_account character varying(50),
    bank_name character varying(255),
    commission_rate numeric(5,2),
    status character varying(50) DEFAULT 'active'::character varying,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    contract_start_date date,
    contract_end_date date,
    monthly_sales_target numeric(15,2),
    yearly_sales_target numeric(15,2),
    CONSTRAINT dealers_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying, 'TERMINATED'::character varying])::text[])))
);


ALTER TABLE public.dealers OWNER TO postgres;

--
-- Name: installment_plans; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.installment_plans (
    plan_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_id uuid,
    customer_id uuid,
    total_amount numeric(15,2) NOT NULL,
    down_payment_amount numeric(15,2) NOT NULL,
    loan_amount numeric(15,2) NOT NULL,
    interest_rate numeric(5,2) NOT NULL,
    loan_term_months integer NOT NULL,
    monthly_payment_amount numeric(15,2) NOT NULL,
    first_payment_date date,
    last_payment_date date,
    plan_status character varying(50) DEFAULT 'active'::character varying,
    finance_company character varying(255),
    contract_number character varying(100),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    invoice_id uuid,
    dealer_id uuid,
    plan_type character varying(50) DEFAULT 'customer'::character varying
);


ALTER TABLE public.installment_plans OWNER TO postgres;

--
-- Name: installment_schedules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.installment_schedules (
    schedule_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    plan_id uuid,
    installment_number integer NOT NULL,
    due_date date NOT NULL,
    amount numeric(15,2) NOT NULL,
    principal_amount numeric(15,2),
    interest_amount numeric(15,2),
    status character varying(50) DEFAULT 'pending'::character varying,
    paid_date date,
    paid_amount numeric(15,2),
    late_fee numeric(15,2) DEFAULT 0,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.installment_schedules OWNER TO postgres;

--
-- Name: orders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.orders (
    order_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_number character varying(100) NOT NULL,
    quotation_id uuid,
    customer_id uuid,
    user_id uuid,
    inventory_id uuid,
    order_date date NOT NULL,
    status character varying(50) DEFAULT 'pending'::character varying,
    total_amount numeric(15,2),
    deposit_amount numeric(15,2),
    balance_amount numeric(15,2),
    payment_method character varying(50),
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    delivery_date date,
    special_requests text,
    order_type character varying(20) DEFAULT 'RETAIL'::character varying NOT NULL,
    payment_status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    delivery_status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    fulfillment_status character varying(50) DEFAULT 'PENDING'::character varying,
    fulfillment_method character varying(50),
    fulfillment_reference_id uuid,
    CONSTRAINT orders_delivery_date_check CHECK (((delivery_date IS NULL) OR (order_date IS NULL) OR (delivery_date >= order_date))),
    CONSTRAINT orders_delivery_status_check CHECK (((delivery_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SCHEDULED'::character varying, 'IN_TRANSIT'::character varying, 'DELIVERED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT orders_fulfillment_status_check CHECK (((fulfillment_status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('PROCESSING'::character varying)::text, ('SHIPPED'::character varying)::text, ('FULFILLED'::character varying)::text, ('CANCELLED'::character varying)::text, ('FAILED'::character varying)::text]))),
    CONSTRAINT orders_order_type_check CHECK (((order_type)::text = ANY ((ARRAY['RETAIL'::character varying, 'WHOLESALE'::character varying, 'DEMO'::character varying, 'TEST_DRIVE'::character varying])::text[]))),
    CONSTRAINT orders_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['PENDING'::character varying, 'PARTIAL'::character varying, 'PAID'::character varying, 'OVERDUE'::character varying, 'REFUNDED'::character varying])::text[]))),
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY (ARRAY[('pending'::character varying)::text, ('quoted'::character varying)::text, ('confirmed'::character varying)::text, ('paid'::character varying)::text, ('delivered'::character varying)::text, ('completed'::character varying)::text, ('rejected'::character varying)::text, ('cancelled'::character varying)::text]))),
    CONSTRAINT orders_total_amount_check CHECK (((total_amount IS NULL) OR (total_amount >= (0)::numeric)))
);


ALTER TABLE public.orders OWNER TO postgres;

--
-- Name: pricing_policies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pricing_policies (
    policy_id uuid DEFAULT gen_random_uuid() NOT NULL,
    policy_name character varying(255) NOT NULL,
    description text,
    policy_type character varying(50) DEFAULT 'standard'::character varying,
    base_price numeric(15,2),
    discount_percent numeric(5,2),
    discount_amount numeric(15,2),
    markup_percent numeric(5,2),
    markup_amount numeric(15,2),
    effective_date date NOT NULL,
    expiry_date date,
    min_quantity integer,
    max_quantity integer,
    customer_type character varying(50),
    region character varying(100),
    status character varying(50) DEFAULT 'active'::character varying,
    priority integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    variant_id integer,
    dealer_id uuid,
    scope character varying(50) DEFAULT 'global'::character varying,
    CONSTRAINT pricing_policies_date_range_check CHECK (((expiry_date IS NULL) OR (effective_date IS NULL) OR (expiry_date >= effective_date)))
);


ALTER TABLE public.pricing_policies OWNER TO postgres;

--
-- Name: promotions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.promotions (
    promotion_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    variant_id integer,
    title character varying(255) NOT NULL,
    description text,
    discount_percent numeric(5,2),
    discount_amount numeric(15,2),
    start_date date NOT NULL,
    end_date date NOT NULL,
    status character varying(50) DEFAULT 'active'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT promotions_date_range_check CHECK ((end_date >= start_date))
);


ALTER TABLE public.promotions OWNER TO postgres;

--
-- Name: quotations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.quotations (
    quotation_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    quotation_number character varying(100) NOT NULL,
    customer_id uuid,
    user_id uuid,
    variant_id integer,
    color_id integer,
    quotation_date date DEFAULT CURRENT_DATE NOT NULL,
    total_price numeric(15,2) NOT NULL,
    discount_amount numeric(15,2) DEFAULT 0,
    final_price numeric(15,2) NOT NULL,
    validity_days integer DEFAULT 7,
    status character varying(50) DEFAULT 'pending'::character varying,
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    accepted_at timestamp(6) without time zone,
    expiry_date date,
    rejected_at timestamp(6) without time zone,
    rejection_reason text
);


ALTER TABLE public.quotations OWNER TO postgres;

--
-- Name: sales_contracts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sales_contracts (
    contract_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    contract_number character varying(100) NOT NULL,
    order_id uuid,
    customer_id uuid,
    user_id uuid,
    contract_date date NOT NULL,
    delivery_date date,
    contract_value numeric(15,2) NOT NULL,
    payment_terms text,
    warranty_period_months integer DEFAULT 24,
    contract_status character varying(50) DEFAULT 'draft'::character varying,
    signed_date date,
    contract_file_url character varying(500),
    contract_file_path character varying(500),
    notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sales_contracts_delivery_date_check CHECK (((delivery_date IS NULL) OR (contract_date IS NULL) OR (delivery_date >= contract_date)))
);


ALTER TABLE public.sales_contracts OWNER TO postgres;

--
-- Name: test_drive_schedules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.test_drive_schedules (
    schedule_id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    notes text,
    preferred_date date NOT NULL,
    preferred_time time(6) without time zone NOT NULL,
    status character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    customer_id uuid,
    variant_id integer
);


ALTER TABLE public.test_drive_schedules OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(100) NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    phone character varying(20),
    address text,
    date_of_birth date,
    profile_image_url character varying(500),
    profile_image_path character varying(500),
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    dealer_id uuid,
    user_type character varying(20) DEFAULT 'DEALER_STAFF'::character varying NOT NULL,
    status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    last_login timestamp without time zone,
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying])::text[]))),
    CONSTRAINT users_user_type_check CHECK (((user_type)::text = ANY ((ARRAY['ADMIN'::character varying, 'EVM_STAFF'::character varying, 'DEALER_MANAGER'::character varying, 'DEALER_STAFF'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: vehicle_brands; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicle_brands (
    brand_id integer NOT NULL,
    brand_name character varying(100) NOT NULL,
    country character varying(100),
    founded_year integer,
    brand_logo_url character varying(500),
    brand_logo_path character varying(500),
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.vehicle_brands OWNER TO postgres;

--
-- Name: vehicle_brands_brand_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vehicle_brands_brand_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_brands_brand_id_seq OWNER TO postgres;

--
-- Name: vehicle_brands_brand_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.vehicle_brands_brand_id_seq OWNED BY public.vehicle_brands.brand_id;


--
-- Name: vehicle_colors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicle_colors (
    color_id integer NOT NULL,
    color_name character varying(100) NOT NULL,
    color_code character varying(20),
    color_swatch_url character varying(500),
    color_swatch_path character varying(500),
    is_active boolean DEFAULT true
);


ALTER TABLE public.vehicle_colors OWNER TO postgres;

--
-- Name: vehicle_colors_color_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vehicle_colors_color_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_colors_color_id_seq OWNER TO postgres;

--
-- Name: vehicle_colors_color_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.vehicle_colors_color_id_seq OWNED BY public.vehicle_colors.color_id;


--
-- Name: vehicle_deliveries; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicle_deliveries (
    delivery_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_id uuid,
    inventory_id uuid,
    customer_id uuid,
    delivery_date date NOT NULL,
    delivery_time time without time zone,
    delivery_address text NOT NULL,
    delivery_contact_name character varying(100),
    delivery_contact_phone character varying(20),
    delivery_status character varying(50) DEFAULT 'scheduled'::character varying,
    delivery_notes text,
    delivered_by uuid,
    delivery_confirmation_date timestamp without time zone,
    customer_signature_url character varying(500),
    customer_signature_path character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    actual_delivery_date date,
    condition character varying(100),
    notes text,
    scheduled_delivery_date date,
    dealer_order_id uuid,
    dealer_order_item_id uuid,
    early_delivery_reason text,
    is_early_delivery boolean,
    CONSTRAINT vehicle_deliveries_delivery_status_check CHECK (((delivery_status)::text = ANY (ARRAY[('pending'::character varying)::text, ('scheduled'::character varying)::text, ('in_transit'::character varying)::text, ('delivered'::character varying)::text, ('cancelled'::character varying)::text])))
);


ALTER TABLE public.vehicle_deliveries OWNER TO postgres;

--
-- Name: vehicle_inventory; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicle_inventory (
    inventory_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    variant_id integer,
    color_id integer,
    warehouse_id uuid,
    warehouse_location character varying(100),
    vin character varying(17),
    chassis_number character varying(50),
    manufacturing_date date,
    arrival_date date,
    status character varying(50) DEFAULT 'available'::character varying,
    cost_price numeric(15,2),
    selling_price numeric(15,2),
    vehicle_images jsonb,
    interior_images jsonb,
    exterior_images jsonb,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    reserved_for_dealer uuid,
    reserved_for_customer uuid,
    condition character varying(20) DEFAULT 'NEW'::character varying,
    reserved_date timestamp without time zone,
    reserved_expiry_date timestamp without time zone,
    version bigint,
    CONSTRAINT chk_vehicle_inventory_exterior_images_valid_json CHECK (((exterior_images IS NULL) OR (jsonb_typeof(exterior_images) = 'object'::text))),
    CONSTRAINT chk_vehicle_inventory_interior_images_valid_json CHECK (((interior_images IS NULL) OR (jsonb_typeof(interior_images) = 'object'::text))),
    CONSTRAINT chk_vehicle_inventory_vehicle_images_valid_json CHECK (((vehicle_images IS NULL) OR (jsonb_typeof(vehicle_images) = 'object'::text))),
    CONSTRAINT vehicle_inventory_condition_check CHECK (((condition)::text = ANY ((ARRAY['NEW'::character varying, 'USED'::character varying, 'DEMO'::character varying, 'DAMAGED'::character varying])::text[]))),
    CONSTRAINT vehicle_inventory_status_check CHECK (((status)::text = ANY (ARRAY[('available'::character varying)::text, ('reserved'::character varying)::text, ('sold'::character varying)::text, ('maintenance'::character varying)::text, ('damaged'::character varying)::text, ('in_transit'::character varying)::text, ('pending_delivery'::character varying)::text])))
);


ALTER TABLE public.vehicle_inventory OWNER TO postgres;

--
-- Name: vehicle_models; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicle_models (
    model_id integer NOT NULL,
    brand_id integer,
    model_name character varying(100) NOT NULL,
    model_year integer NOT NULL,
    vehicle_type character varying(50),
    description text,
    specifications jsonb,
    model_image_url character varying(500),
    model_image_path character varying(500),
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_vehicle_models_specifications_valid_json CHECK (((specifications IS NULL) OR (jsonb_typeof(specifications) = 'object'::text)))
);


ALTER TABLE public.vehicle_models OWNER TO postgres;

--
-- Name: vehicle_models_model_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vehicle_models_model_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_models_model_id_seq OWNER TO postgres;

--
-- Name: vehicle_models_model_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.vehicle_models_model_id_seq OWNED BY public.vehicle_models.model_id;


--
-- Name: vehicle_variants; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicle_variants (
    variant_id integer NOT NULL,
    model_id integer NOT NULL,
    variant_name character varying(100) NOT NULL,
    battery_capacity numeric(8,2),
    range_km integer,
    power_kw numeric(8,2),
    acceleration_0_100 numeric(4,2),
    top_speed integer,
    charging_time_fast integer,
    charging_time_slow integer,
    price_base numeric(15,2),
    variant_image_url character varying(500),
    variant_image_path character varying(500),
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.vehicle_variants OWNER TO postgres;

--
-- Name: vehicle_variants_variant_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vehicle_variants_variant_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicle_variants_variant_id_seq OWNER TO postgres;

--
-- Name: vehicle_variants_variant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.vehicle_variants_variant_id_seq OWNED BY public.vehicle_variants.variant_id;


--
-- Name: warehouse; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.warehouse (
    warehouse_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    warehouse_name character varying(255) DEFAULT 'Main EV Warehouse'::character varying NOT NULL,
    warehouse_code character varying(50) DEFAULT 'MAIN_WAREHOUSE'::character varying NOT NULL,
    address text NOT NULL,
    city character varying(100),
    province character varying(100),
    postal_code character varying(20),
    phone character varying(20),
    email character varying(255),
    capacity integer,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.warehouse OWNER TO postgres;

--
-- Name: vehicle_brands brand_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_brands ALTER COLUMN brand_id SET DEFAULT nextval('public.vehicle_brands_brand_id_seq'::regclass);


--
-- Name: vehicle_colors color_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_colors ALTER COLUMN color_id SET DEFAULT nextval('public.vehicle_colors_color_id_seq'::regclass);


--
-- Name: vehicle_models model_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_models ALTER COLUMN model_id SET DEFAULT nextval('public.vehicle_models_model_id_seq'::regclass);


--
-- Name: vehicle_variants variant_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_variants ALTER COLUMN variant_id SET DEFAULT nextval('public.vehicle_variants_variant_id_seq'::regclass);


--
-- Data for Name: appointments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.appointments (appointment_id, customer_id, staff_id, appointment_type, title, description, appointment_date, duration_minutes, location, status, notes, created_at, updated_at, variant_id) FROM stdin;
1d956e90-14db-4927-a883-8a8bd53994c8	\N	\N	test_drive	Lái thử xe - Test Customer	\N	2024-12-20 03:00:00	60	\N	scheduled	Test appointment\nCustomer: Test Customer (test@example.com, 0123456789), Variant ID: 1	2025-10-26 15:24:30.955333	2025-10-26 15:24:30.955333	\N
af293c23-38d8-4106-b37b-f7195dcde0ed	\N	\N	delivery	Nhận xe - Test Customer	\N	2024-12-25 07:00:00	60	\N	scheduled	Test delivery appointment\nCustomer: Test Customer (test@example.com, 0123456789), Order ID: 48bbd74e-ce34-4f48-9b26-eb9e0c9de16a, Address: 123 Test Street, Ho Chi Minh City	2025-10-26 15:29:47.056141	2025-10-26 15:29:47.056141	\N
df6771fa-94c7-46ba-9c1d-a05ea7562a0a	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:31:25	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-153124)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 893f2803-e4d5-499e-be93-cac64c551002, Address: 123 Duong ABC, Quan 1	2025-11-06 08:31:25.487934	2025-11-06 08:31:25.487934	\N
75d05f3d-7b2f-416b-a976-c963dc42cfd7	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:31:42	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-153141)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 8ea4068b-d268-45b7-abde-7608f32ccb50, Address: 123 Duong ABC, Quan 1	2025-11-06 08:31:42.514271	2025-11-06 08:31:42.514271	\N
8889bb38-fb49-42a5-9448-1a5c56f75d91	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:32:03	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-153202)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 6618beaf-bb39-4e99-8edc-7e347bdabbbc, Address: 123 Duong ABC, Quan 1	2025-11-06 08:32:03.875009	2025-11-06 08:32:03.875009	\N
4f898e76-911c-40c5-b10f-c725a28793b9	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:43:30	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-154328)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 8e41ea67-0058-483d-b2ef-7c26b10287c7, Address: 123 Duong ABC, Quan 1	2025-11-06 08:43:30.428008	2025-11-06 08:43:30.428008	\N
79dd563c-a848-4d0b-a7bb-630f6ff36dfd	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:51:42	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-155141)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 96b0a6b7-7b44-45fc-8a42-161f4130a6e8, Address: 123 Duong ABC, Quan 1	2025-11-06 08:51:42.612976	2025-11-06 08:51:42.612976	\N
56a046d7-607f-4023-a82b-e26ec708af68	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:52:39	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-155237)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 58fbcd7d-a9ee-4999-b160-6f29fd508c64, Address: 123 Duong ABC, Quan 1	2025-11-06 08:52:39.269589	2025-11-06 08:52:39.269589	\N
836b78c4-6a41-47f9-8a89-957a9059992c	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:55:57	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-155556)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 21eed8bb-f935-45ac-9e04-e40ce7c76f0a, Address: 123 Duong ABC, Quan 1	2025-11-06 08:55:57.752493	2025-11-06 08:55:57.752493	\N
9ec0fdf9-b56b-4d1b-afe2-a2d9eb8e1314	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:56:55	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-155653)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: a5192104-2293-4a4e-ba65-a1a72528d97b, Address: 123 Duong ABC, Quan 1	2025-11-06 08:56:55.149672	2025-11-06 08:56:55.149672	\N
6d71a4f6-d320-4d25-a944-a484e253949f	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:57:10	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-155709)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 47816a47-8073-40da-b022-c771ab8bede7, Address: 123 Duong ABC, Quan 1	2025-11-06 08:57:10.878337	2025-11-06 08:57:10.878337	\N
c6f6fa6e-0e7c-462e-a657-711636865529	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 08:57:25	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-155724)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: b38623ee-3867-4701-a472-6609bf26f6d3, Address: 123 Duong ABC, Quan 1	2025-11-06 08:57:25.691082	2025-11-06 08:57:25.691082	\N
23699c24-3d06-4b18-aa82-563da59c07a1	\N	\N	delivery	Nhận xe - Nguyá»…n VÄƒn Test	\N	2025-11-13 09:00:19	60	\N	scheduled	Giao hang trong gio hanh chinh - Test (TEST-20251106-160018)\nCustomer: Nguyá»…n VÄƒn Test (test@example.com, 0912345678), Order ID: 401c4722-1b50-4a9c-a125-10aa8bc4605e, Address: 123 Duong ABC, Quan 1	2025-11-06 09:00:19.714516	2025-11-06 09:00:19.714516	\N
\.


--
-- Data for Name: customer_feedbacks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.customer_feedbacks (feedback_id, customer_id, order_id, rating, feedback_type, message, response, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: customer_invoices; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.customer_invoices (invoice_id, created_at, discount_amount, due_date, invoice_date, invoice_number, notes, payment_terms_days, quotation_id, status, subtotal, tax_amount, total_amount, updated_at, customer_id, evm_staff_id, order_id) FROM stdin;
\.


--
-- Data for Name: customer_payments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.customer_payments (payment_id, order_id, customer_id, payment_number, payment_date, amount, payment_type, payment_method, reference_number, status, processed_by, notes, created_at) FROM stdin;
\.


--
-- Data for Name: customers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.customers (customer_id, first_name, last_name, email, phone, date_of_birth, address, city, province, postal_code, credit_score, preferred_contact_method, notes, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: dealer_contracts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_contracts (contract_id, contract_number, contract_type, start_date, end_date, territory, commission_rate, minimum_sales_target, contract_status, signed_date, contract_file_url, contract_file_path, terms_and_conditions, created_at, updated_at, dealer_id, monthly_target, yearly_target) FROM stdin;
\.


--
-- Data for Name: dealer_discount_policies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_discount_policies (policy_id, created_at, description, discount_amount, discount_percent, end_date, policy_name, start_date, status, updated_at, variant_id) FROM stdin;
\.


--
-- Data for Name: dealer_installment_plans; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_installment_plans (plan_id, contract_number, created_at, down_payment_amount, finance_company, first_payment_date, interest_rate, last_payment_date, loan_amount, loan_term_months, monthly_payment_amount, plan_status, total_amount, invoice_id) FROM stdin;
\.


--
-- Data for Name: dealer_installment_schedules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_installment_schedules (schedule_id, amount, created_at, due_date, installment_number, interest_amount, late_fee, notes, paid_amount, paid_date, principal_amount, status, plan_id) FROM stdin;
\.


--
-- Data for Name: dealer_invoices; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_invoices (invoice_id, invoice_number, dealer_order_id, evm_staff_id, invoice_date, due_date, subtotal, tax_amount, discount_amount, total_amount, status, payment_terms_days, notes, created_at, updated_at, quotation_id) FROM stdin;
c3f451ff-60cd-42b7-aac0-87dfe80e2275	TEST-INVOICE-001	e31c5e36-4bd6-4708-b4c8-fa59d161a5b8	\N	2025-10-29	2025-11-28	1000000000.00	0.00	0.00	1000000000.00	issued	30	\N	2025-10-29 16:58:33.656844	2025-10-29 16:58:33.656844	\N
0fc49651-2013-4fb9-a5e1-a5c8452706ad	INV-1762340153877	8cdf0263-d3d2-43e1-afe5-d09875203278	6f2431b7-10c9-4d61-b612-33e11b923752	2025-11-05	2025-12-05	1127000000.00	0.00	0.00	1127000000.00	paid	30	Generated from quotation: DQ-1762340153790	2025-11-05 10:55:53.882093	2025-11-05 17:55:53.964309	60ce0f89-8aab-493c-a11d-fa3d077960d6
d3bb7eb2-c6f1-4be7-8791-43918d2c932b	INV-1762340180607	8cdf0263-d3d2-43e1-afe5-d09875203278	6f2431b7-10c9-4d61-b612-33e11b923752	2025-11-05	2025-12-05	1127000000.00	0.00	0.00	1127000000.00	issued	30	Generated from quotation: DQ-1762340180553	2025-11-05 10:56:20.611761	2025-11-05 10:56:20.611761	fd3e0e86-f1b8-46c0-a648-6e41bed3bd00
19708271-0ec3-47a7-a066-0f53a1e6e802	INV-1762340250827	8cdf0263-d3d2-43e1-afe5-d09875203278	6f2431b7-10c9-4d61-b612-33e11b923752	2025-11-05	2025-12-05	1127000000.00	0.00	0.00	1127000000.00	paid	30	Generated from quotation: DQ-1762340250766	2025-11-05 10:57:30.832359	2025-11-05 17:57:30.898303	bd4f437d-4847-454d-abb1-b0807c77e750
\.


--
-- Data for Name: dealer_order_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_order_items (item_id, created_at, discount_amount, discount_percentage, final_price, notes, quantity, status, total_price, unit_price, updated_at, color_id, dealer_order_id, variant_id) FROM stdin;
0f63f6e6-8585-4e49-81a8-9f1acb682614	2025-11-05 09:57:33.268096	0.00	\N	1127000000.00	\N	1	CONFIRMED	1127000000.00	1127000000.00	2025-11-05 17:18:46.117157	10	8cdf0263-d3d2-43e1-afe5-d09875203278	51
\.


--
-- Data for Name: dealer_orders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_orders (dealer_order_id, dealer_order_number, evm_staff_id, order_date, expected_delivery_date, total_quantity, total_amount, status, priority, notes, created_at, updated_at, approved_at, approved_by, rejection_reason, dealer_id, order_type, approval_status, payment_terms, delivery_terms, discount_applied, discount_reason) FROM stdin;
8cdf0263-d3d2-43e1-afe5-d09875203278	DO-1762336653242	\N	2025-11-05	2025-12-05	1	1127000000.00	WAITING_FOR_QUOTATION	NORMAL	Test - Buoc 12	2025-11-05 09:57:33.254102	2025-11-05 17:25:24.216104	2025-11-05 10:18:46.125114	6f2431b7-10c9-4d61-b612-33e11b923752	\N	42a9c22c-5817-438c-9aea-859a99c33f2f	PURCHASE	APPROVED	NET_30	FOB_FACTORY	0.00	\N
e31c5e36-4bd6-4708-b4c8-fa59d161a5b8	TEST-DEALER-ORDER-001	\N	2025-10-29	\N	1	1000000000.00	WAITING_FOR_QUOTATION	normal	\N	2025-10-29 16:58:33.656844	2025-11-05 17:34:00.59353	\N	\N	\N	42a9c22c-5817-438c-9aea-859a99c33f2f	PURCHASE	APPROVED	NET_30	FOB_FACTORY	0.00	\N
\.


--
-- Data for Name: dealer_payments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_payments (payment_id, invoice_id, payment_number, payment_date, amount, payment_type, reference_number, status, notes, created_at) FROM stdin;
31b8fc5c-d518-4df0-8a56-bb0c7cef3d5f	0fc49651-2013-4fb9-a5e1-a5c8452706ad	PAY-1762340153957	2025-11-05	1127000000.00	BANK_TRANSFER	TX1762340153	completed	Test payment	2025-11-05 10:55:53.959093
9cf90746-61c1-4ba0-bfff-272bf0bc6582	19708271-0ec3-47a7-a066-0f53a1e6e802	PAY-1762340250890	2025-11-05	1127000000.00	BANK_TRANSFER	TX1762340250	completed	Test payment	2025-11-05 10:57:30.894362
\.


--
-- Data for Name: dealer_quotation_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_quotation_items (item_id, quotation_id, variant_id, color_id, quantity, unit_price, discount_percentage, discount_amount, total_price, notes, created_at, updated_at) FROM stdin;
c2020e44-2046-4afd-9429-395fc343f396	60ce0f89-8aab-493c-a11d-fa3d077960d6	51	10	1	1127000000.00	0.00	0.00	1127000000.00	\N	2025-11-05 10:55:53.818092	2025-11-05 10:55:53.818092
65e79ecc-c58b-4b28-b594-fb25cad912c7	fd3e0e86-f1b8-46c0-a648-6e41bed3bd00	51	10	1	1127000000.00	0.00	0.00	1127000000.00	\N	2025-11-05 10:56:20.562762	2025-11-05 10:56:20.562762
2badf012-d996-4cf8-a283-bc2c8771b341	bd4f437d-4847-454d-abb1-b0807c77e750	51	10	1	1127000000.00	0.00	0.00	1127000000.00	\N	2025-11-05 10:57:30.777033	2025-11-05 10:57:30.777033
\.


--
-- Data for Name: dealer_quotations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_quotations (quotation_id, quotation_number, dealer_id, dealer_order_id, evm_staff_id, quotation_date, validity_days, expiry_date, subtotal, tax_amount, discount_amount, discount_percentage, total_amount, status, payment_terms, delivery_terms, expected_delivery_date, accepted_at, rejected_at, rejection_reason, notes, created_at, updated_at) FROM stdin;
60ce0f89-8aab-493c-a11d-fa3d077960d6	DQ-1762340153790	42a9c22c-5817-438c-9aea-859a99c33f2f	8cdf0263-d3d2-43e1-afe5-d09875203278	6f2431b7-10c9-4d61-b612-33e11b923752	2025-11-05	30	2025-12-05	1127000000.00	0.00	0.00	0.00	1127000000.00	converted	NET_30	FOB_FACTORY	2025-12-05	2025-11-05 10:55:53.877092	\N	\N	\N	2025-11-05 10:55:53.795094	2025-11-05 17:55:53.875958
fd3e0e86-f1b8-46c0-a648-6e41bed3bd00	DQ-1762340180553	42a9c22c-5817-438c-9aea-859a99c33f2f	8cdf0263-d3d2-43e1-afe5-d09875203278	6f2431b7-10c9-4d61-b612-33e11b923752	2025-11-05	30	2025-12-05	1127000000.00	0.00	0.00	0.00	1127000000.00	converted	NET_30	FOB_FACTORY	2025-12-05	2025-11-05 10:56:20.607759	\N	\N	\N	2025-11-05 10:56:20.556762	2025-11-05 17:56:20.607456
bd4f437d-4847-454d-abb1-b0807c77e750	DQ-1762340250766	42a9c22c-5817-438c-9aea-859a99c33f2f	8cdf0263-d3d2-43e1-afe5-d09875203278	6f2431b7-10c9-4d61-b612-33e11b923752	2025-11-05	30	2025-12-05	1127000000.00	0.00	0.00	0.00	1127000000.00	converted	NET_30	FOB_FACTORY	2025-12-05	2025-11-05 10:57:30.82736	\N	\N	\N	2025-11-05 10:57:30.773034	2025-11-05 17:57:30.826458
\.


--
-- Data for Name: dealer_targets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealer_targets (target_id, target_year, target_month, target_type, target_amount, target_quantity, achieved_amount, achieved_quantity, target_status, notes, created_at, updated_at, dealer_id, target_scope) FROM stdin;
\.


--
-- Data for Name: dealers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dealers (dealer_id, dealer_code, dealer_name, contact_person, email, phone, address, city, province, postal_code, dealer_type, license_number, tax_code, bank_account, bank_name, commission_rate, status, notes, created_at, updated_at, contract_start_date, contract_end_date, monthly_sales_target, yearly_sales_target) FROM stdin;
42a9c22c-5817-438c-9aea-859a99c33f2f	EVD001	EV Đại lý 1 test	\N	evd001@gmail.com	0987654321	1/1a abc	HCM	\N	70000	authorized	AM02	0123019788	\N	\N	1.00	ACTIVE	\N	2025-11-03 12:44:13.47997	2025-11-03 19:49:09.718835	\N	\N	\N	\N
\.


--
-- Data for Name: installment_plans; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.installment_plans (plan_id, order_id, customer_id, total_amount, down_payment_amount, loan_amount, interest_rate, loan_term_months, monthly_payment_amount, first_payment_date, last_payment_date, plan_status, finance_company, contract_number, created_at, invoice_id, dealer_id, plan_type) FROM stdin;
\.


--
-- Data for Name: installment_schedules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.installment_schedules (schedule_id, plan_id, installment_number, due_date, amount, principal_amount, interest_amount, status, paid_date, paid_amount, late_fee, notes, created_at) FROM stdin;
\.


--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.orders (order_id, order_number, quotation_id, customer_id, user_id, inventory_id, order_date, status, total_amount, deposit_amount, balance_amount, payment_method, notes, created_at, updated_at, delivery_date, special_requests, order_type, payment_status, delivery_status, fulfillment_status, fulfillment_method, fulfillment_reference_id) FROM stdin;
\.


--
-- Data for Name: pricing_policies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.pricing_policies (policy_id, policy_name, description, policy_type, base_price, discount_percent, discount_amount, markup_percent, markup_amount, effective_date, expiry_date, min_quantity, max_quantity, customer_type, region, status, priority, created_at, updated_at, variant_id, dealer_id, scope) FROM stdin;
\.


--
-- Data for Name: promotions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.promotions (promotion_id, variant_id, title, description, discount_percent, discount_amount, start_date, end_date, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: quotations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.quotations (quotation_id, quotation_number, customer_id, user_id, variant_id, color_id, quotation_date, total_price, discount_amount, final_price, validity_days, status, notes, created_at, updated_at, accepted_at, expiry_date, rejected_at, rejection_reason) FROM stdin;
\.


--
-- Data for Name: sales_contracts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sales_contracts (contract_id, contract_number, order_id, customer_id, user_id, contract_date, delivery_date, contract_value, payment_terms, warranty_period_months, contract_status, signed_date, contract_file_url, contract_file_path, notes, created_at, updated_at) FROM stdin;
298948eb-8577-4264-a6f7-07d365f93a97	HD-TEST-20251106152157	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-152157)	2025-11-06 08:21:57.818464	2025-11-06 08:21:57.818464
b63269bb-a357-46cb-b24f-bd41e49732d8	HD-TEST-20251106153125	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-153124)	2025-11-06 08:31:25.454814	2025-11-06 08:31:25.454814
6a8fb108-ed8d-4db2-8e41-33e061d1a6ba	HD-TEST-20251106153142	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-153141)	2025-11-06 08:31:42.49662	2025-11-06 08:31:42.49662
cd0d4df6-ff78-4b9c-9e41-b5dfdf4da137	HD-TEST-20251106153203	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-153202)	2025-11-06 08:32:03.85744	2025-11-06 08:32:03.85744
bf4b44a1-04b8-426a-a65f-8d6a4b522ff1	HD-TEST-20251106154330	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-154328)	2025-11-06 08:43:30.409968	2025-11-06 08:43:30.409968
d020d68b-d3de-43c1-8378-c38700fbbe8d	HD-TEST-20251106155142	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-155141)	2025-11-06 08:51:42.593925	2025-11-06 08:51:42.594926
3b7277c5-8d1b-4e08-84fd-c4afecd35118	HD-TEST-20251106155239	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-155237)	2025-11-06 08:52:39.251501	2025-11-06 08:52:39.251501
b4ceeb4a-d252-4af0-9d62-30517aa1f698	HD-TEST-20251106155557	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-155556)	2025-11-06 08:55:57.734911	2025-11-06 08:55:57.734911
0c97ae09-efd8-4a89-bcef-ade32e5ed92b	HD-TEST-20251106155655	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-155653)	2025-11-06 08:56:55.132084	2025-11-06 08:56:55.132084
10fbef30-9fee-4bde-ae99-37d050ced476	HD-TEST-20251106155710	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-155709)	2025-11-06 08:57:10.860279	2025-11-06 08:57:10.860279
43d2725d-25eb-400f-b267-d1c5986b7c64	HD-TEST-20251106155725	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-155724)	2025-11-06 08:57:25.67374	2025-11-06 08:57:25.67374
f6601c5a-398a-4ca7-b2d4-dc4fcd678a0b	HD-TEST-20251106160019	\N	\N	\N	2025-11-06	2025-11-21	1180000000.00	Thanh toan du 100%	24	draft	\N	\N	\N	Ghi chu hop dong - Test (TEST-20251106-160018)	2025-11-06 09:00:19.698077	2025-11-06 09:00:19.698077
\.


--
-- Data for Name: test_drive_schedules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.test_drive_schedules (schedule_id, created_at, notes, preferred_date, preferred_time, status, updated_at, customer_id, variant_id) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, username, email, password_hash, first_name, last_name, phone, address, date_of_birth, profile_image_url, profile_image_path, is_active, created_at, updated_at, dealer_id, user_type, status, last_login) FROM stdin;
6f2431b7-10c9-4d61-b612-33e11b923752	admin	admin@evdealer.com	$2a$10$twGkmusRXuBWmxF7.j04n.jt7BMv2W1TgcVNGiZNLAlJ68vGWU7Ne	Test	Administrator	0123456789	System Address	\N	\N	\N	t	2025-10-16 10:16:03.680024	2025-10-29 16:15:11.727302	\N	ADMIN	ACTIVE	\N
\.


--
-- Data for Name: vehicle_brands; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicle_brands (brand_id, brand_name, country, founded_year, brand_logo_url, brand_logo_path, is_active, created_at) FROM stdin;
8	Tesla test	USA	2003	/uploads/brands/tesla_test/91a75456-b195-45a7-82d9-19c535c14972.png	brands/tesla_test/91a75456-b195-45a7-82d9-19c535c14972.png	t	2025-11-03 07:37:12.466565
9	BYD	China	2003	/uploads/brands/byd/54391b19-8ed3-4384-b936-62ad35be4693.jpg	brands/byd/54391b19-8ed3-4384-b936-62ad35be4693.jpg	t	2025-11-05 04:24:41.091735
\.


--
-- Data for Name: vehicle_colors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicle_colors (color_id, color_name, color_code, color_swatch_url, color_swatch_path, is_active) FROM stdin;
10	RED	RED001	\N	\N	t
9	BLUE	BLUE01			t
\.


--
-- Data for Name: vehicle_deliveries; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicle_deliveries (delivery_id, order_id, inventory_id, customer_id, delivery_date, delivery_time, delivery_address, delivery_contact_name, delivery_contact_phone, delivery_status, delivery_notes, delivered_by, delivery_confirmation_date, customer_signature_url, customer_signature_path, created_at, updated_at, actual_delivery_date, condition, notes, scheduled_delivery_date, dealer_order_id, dealer_order_item_id, early_delivery_reason, is_early_delivery) FROM stdin;
4bfc691c-ca3d-4f52-95e3-e3dd4be0c0e9	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:35:02.268236	2025-11-05 10:35:02.268236	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
0573a038-41ce-415b-9d8c-b4055cb23489	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:35:31.502033	2025-11-05 10:35:31.502033	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
3881b9ce-a138-48a6-be94-243dba961ca7	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:53:34.014558	2025-11-05 10:53:34.014558	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
7cf96cbb-f08a-4ac9-87ea-79c98752b6a1	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:54:20.788618	2025-11-05 10:54:20.788618	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
160d9612-5972-43b8-a45b-b5ca22208317	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:54:47.67989	2025-11-05 10:54:47.67989	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
baafb916-680c-47c2-a17d-5c51aa202a91	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:55:26.467934	2025-11-05 10:55:26.467934	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
21d90f1a-ff1f-48bd-8405-1f959b446f78	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:55:53.993093	2025-11-05 10:55:53.993093	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
ad343fe9-04b9-4719-8b4a-26d2fdab150a	\N	\N	\N	2025-12-05	\N	Dia chi dai ly	\N	\N	scheduled	\N	\N	\N	\N	\N	2025-11-05 10:57:30.938361	2025-11-05 10:57:30.938361	\N	\N	Giao hang trong gio hanh chinh	2025-12-05	8cdf0263-d3d2-43e1-afe5-d09875203278	0f63f6e6-8585-4e49-81a8-9f1acb682614	\N	\N
\.


--
-- Data for Name: vehicle_inventory; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicle_inventory (inventory_id, variant_id, color_id, warehouse_id, warehouse_location, vin, chassis_number, manufacturing_date, arrival_date, status, cost_price, selling_price, vehicle_images, interior_images, exterior_images, created_at, updated_at, reserved_for_dealer, reserved_for_customer, condition, reserved_date, reserved_expiry_date, version) FROM stdin;
cde0fcc0-8b99-4018-bb4a-661453dcb567	51	10	f9e3d37d-b23d-4c21-9666-4636667bdd7c	\N	1HGBH41JXMN109188	\N	\N	\N	available	\N	\N	\N	\N	\N	2025-11-03 12:59:52.966529	2025-11-09 15:59:46.741261	\N	\N	NEW	2025-11-06 07:48:38.27816	\N	\N
561f7afa-912e-46f5-8c9c-092a19656752	51	9	f9e3d37d-b23d-4c21-9666-4636667bdd7c	\N	1HGBH41JXMN109189	\N	\N	\N	reserved	\N	\N	\N	\N	\N	2025-11-04 06:09:35.627478	2025-11-09 15:59:46.984299	\N	\N	NEW	2025-11-06 10:29:20.458834	\N	\N
0a72dd3b-e918-4fc8-abb6-86d9c387b614	53	10	f9e3d37d-b23d-4c21-9666-4636667bdd7c	Warehouse A, Bay 1	1HGBH41JXMN739720	CH739720	2025-01-15	\N	reserved	1000000000.00	1200000000.00	\N	\N	\N	2025-11-05 09:04:48.710974	2025-11-09 16:01:13.656003	\N	\N	NEW	2025-11-06 09:00:18.464163	\N	\N
\.


--
-- Data for Name: vehicle_models; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicle_models (model_id, brand_id, model_name, model_year, vehicle_type, description, specifications, model_image_url, model_image_path, is_active, created_at) FROM stdin;
28	9	BYD SEALION 6	2025	SUV	Đây là mẫu SUV hạng C sử dụng công nghệ DM-i Super Hybrid độc quyền của BYD, mang đến trải nghiệm vận hành êm ái, tiết kiệm nhiên liệu và khả năng di chuyển thuần điện lên đến 100 km	\N	\N	\N	t	2025-11-05 04:30:09.149027
25	8	Tesla Model 3	2017	SEDAN	Tesla Model 3 nổi bật với hiệu suất mạnh mẽ, phạm vi hoạt động tốt và chi phí vận hành tiết kiệm, là mẫu xe điện phổ biến trên thị trường hiện nay.	\N	\N	\N	t	2025-11-03 07:49:42.193365
\.


--
-- Data for Name: vehicle_variants; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicle_variants (variant_id, model_id, variant_name, battery_capacity, range_km, power_kw, acceleration_0_100, top_speed, charging_time_fast, charging_time_slow, price_base, variant_image_url, variant_image_path, is_active, created_at) FROM stdin;
51	25	Model 3 Long Range RWD	60.00	554	202.00	6.10	225	30	600	1127000000.00	/uploads/variants/model_3_long_range_rwd/78108c48-4feb-40c7-8a8f-9ea4aeac170a.jpg	variants/model_3_long_range_rwd/78108c48-4feb-40c7-8a8f-9ea4aeac170a.jpg	t	2025-11-03 10:42:19.164582
53	28	Dynamic	18.30	100	159.00	7.90	180	30	240	839000000.00	/uploads/variants/3270439e-8346-48b5-94e1-479e902281d6.jpg	variants/3270439e-8346-48b5-94e1-479e902281d6.jpg	t	2025-11-05 05:02:49.865792
\.


--
-- Data for Name: warehouse; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.warehouse (warehouse_id, warehouse_name, warehouse_code, address, city, province, postal_code, phone, email, capacity, is_active, created_at, updated_at) FROM stdin;
f9e3d37d-b23d-4c21-9666-4636667bdd7c	Kho 1	W001	1/1a abc	HCM	HCM	70000	0987654321	kho001@gmail.com	1000	t	2025-11-03 12:55:55.127747	2025-11-03 12:55:55.127747
\.


--
-- Name: vehicle_brands_brand_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.vehicle_brands_brand_id_seq', 9, true);


--
-- Name: vehicle_colors_color_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.vehicle_colors_color_id_seq', 10, true);


--
-- Name: vehicle_models_model_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.vehicle_models_model_id_seq', 28, true);


--
-- Name: vehicle_variants_variant_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.vehicle_variants_variant_id_seq', 55, true);


--
-- Name: appointments appointments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT appointments_pkey PRIMARY KEY (appointment_id);


--
-- Name: customer_feedbacks customer_feedbacks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_feedbacks
    ADD CONSTRAINT customer_feedbacks_pkey PRIMARY KEY (feedback_id);


--
-- Name: customer_invoices customer_invoices_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_invoices
    ADD CONSTRAINT customer_invoices_pkey PRIMARY KEY (invoice_id);


--
-- Name: customer_payments customer_payments_payment_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_payments
    ADD CONSTRAINT customer_payments_payment_number_key UNIQUE (payment_number);


--
-- Name: customer_payments customer_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_payments
    ADD CONSTRAINT customer_payments_pkey PRIMARY KEY (payment_id);


--
-- Name: customers customers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (customer_id);


--
-- Name: dealer_contracts dealer_contracts_contract_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_contracts
    ADD CONSTRAINT dealer_contracts_contract_number_key UNIQUE (contract_number);


--
-- Name: dealer_contracts dealer_contracts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_contracts
    ADD CONSTRAINT dealer_contracts_pkey PRIMARY KEY (contract_id);


--
-- Name: dealer_discount_policies dealer_discount_policies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_discount_policies
    ADD CONSTRAINT dealer_discount_policies_pkey PRIMARY KEY (policy_id);


--
-- Name: dealer_installment_plans dealer_installment_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_installment_plans
    ADD CONSTRAINT dealer_installment_plans_pkey PRIMARY KEY (plan_id);


--
-- Name: dealer_installment_schedules dealer_installment_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_installment_schedules
    ADD CONSTRAINT dealer_installment_schedules_pkey PRIMARY KEY (schedule_id);


--
-- Name: dealer_invoices dealer_invoices_invoice_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_invoices
    ADD CONSTRAINT dealer_invoices_invoice_number_key UNIQUE (invoice_number);


--
-- Name: dealer_invoices dealer_invoices_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_invoices
    ADD CONSTRAINT dealer_invoices_pkey PRIMARY KEY (invoice_id);


--
-- Name: dealer_order_items dealer_order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_order_items
    ADD CONSTRAINT dealer_order_items_pkey PRIMARY KEY (item_id);


--
-- Name: dealer_orders dealer_orders_dealer_order_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_orders
    ADD CONSTRAINT dealer_orders_dealer_order_number_key UNIQUE (dealer_order_number);


--
-- Name: dealer_orders dealer_orders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_orders
    ADD CONSTRAINT dealer_orders_pkey PRIMARY KEY (dealer_order_id);


--
-- Name: dealer_payments dealer_payments_payment_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_payments
    ADD CONSTRAINT dealer_payments_payment_number_key UNIQUE (payment_number);


--
-- Name: dealer_payments dealer_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_payments
    ADD CONSTRAINT dealer_payments_pkey PRIMARY KEY (payment_id);


--
-- Name: dealer_quotation_items dealer_quotation_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotation_items
    ADD CONSTRAINT dealer_quotation_items_pkey PRIMARY KEY (item_id);


--
-- Name: dealer_quotations dealer_quotations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotations
    ADD CONSTRAINT dealer_quotations_pkey PRIMARY KEY (quotation_id);


--
-- Name: dealer_quotations dealer_quotations_quotation_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotations
    ADD CONSTRAINT dealer_quotations_quotation_number_key UNIQUE (quotation_number);


--
-- Name: dealer_targets dealer_targets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_targets
    ADD CONSTRAINT dealer_targets_pkey PRIMARY KEY (target_id);


--
-- Name: dealers dealers_dealer_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealers
    ADD CONSTRAINT dealers_dealer_code_key UNIQUE (dealer_code);


--
-- Name: dealers dealers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealers
    ADD CONSTRAINT dealers_pkey PRIMARY KEY (dealer_id);


--
-- Name: installment_plans installment_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.installment_plans
    ADD CONSTRAINT installment_plans_pkey PRIMARY KEY (plan_id);


--
-- Name: installment_schedules installment_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.installment_schedules
    ADD CONSTRAINT installment_schedules_pkey PRIMARY KEY (schedule_id);


--
-- Name: orders orders_order_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_number_key UNIQUE (order_number);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);


--
-- Name: pricing_policies pricing_policies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pricing_policies
    ADD CONSTRAINT pricing_policies_pkey PRIMARY KEY (policy_id);


--
-- Name: promotions promotions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_pkey PRIMARY KEY (promotion_id);


--
-- Name: quotations quotations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quotations
    ADD CONSTRAINT quotations_pkey PRIMARY KEY (quotation_id);


--
-- Name: quotations quotations_quotation_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quotations
    ADD CONSTRAINT quotations_quotation_number_key UNIQUE (quotation_number);


--
-- Name: sales_contracts sales_contracts_contract_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_contracts
    ADD CONSTRAINT sales_contracts_contract_number_key UNIQUE (contract_number);


--
-- Name: sales_contracts sales_contracts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_contracts
    ADD CONSTRAINT sales_contracts_pkey PRIMARY KEY (contract_id);


--
-- Name: test_drive_schedules test_drive_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_drive_schedules
    ADD CONSTRAINT test_drive_schedules_pkey PRIMARY KEY (schedule_id);


--
-- Name: customer_invoices uk_7xiqc2huare937ulgw9dv0ac4; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_invoices
    ADD CONSTRAINT uk_7xiqc2huare937ulgw9dv0ac4 UNIQUE (invoice_number);


--
-- Name: vehicle_models ukgexpl1erlcv1p14voml8ytdv; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_models
    ADD CONSTRAINT ukgexpl1erlcv1p14voml8ytdv UNIQUE (brand_id, model_name, model_year);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: vehicle_brands vehicle_brands_brand_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_brands
    ADD CONSTRAINT vehicle_brands_brand_name_key UNIQUE (brand_name);


--
-- Name: vehicle_brands vehicle_brands_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_brands
    ADD CONSTRAINT vehicle_brands_pkey PRIMARY KEY (brand_id);


--
-- Name: vehicle_colors vehicle_colors_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_colors
    ADD CONSTRAINT vehicle_colors_pkey PRIMARY KEY (color_id);


--
-- Name: vehicle_deliveries vehicle_deliveries_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_deliveries
    ADD CONSTRAINT vehicle_deliveries_pkey PRIMARY KEY (delivery_id);


--
-- Name: vehicle_inventory vehicle_inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_inventory
    ADD CONSTRAINT vehicle_inventory_pkey PRIMARY KEY (inventory_id);


--
-- Name: vehicle_inventory vehicle_inventory_vin_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_inventory
    ADD CONSTRAINT vehicle_inventory_vin_key UNIQUE (vin);


--
-- Name: vehicle_models vehicle_models_brand_id_model_name_model_year_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_models
    ADD CONSTRAINT vehicle_models_brand_id_model_name_model_year_key UNIQUE (brand_id, model_name, model_year);


--
-- Name: vehicle_models vehicle_models_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_models
    ADD CONSTRAINT vehicle_models_pkey PRIMARY KEY (model_id);


--
-- Name: vehicle_variants vehicle_variants_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_variants
    ADD CONSTRAINT vehicle_variants_pkey PRIMARY KEY (variant_id);


--
-- Name: warehouse warehouse_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.warehouse
    ADD CONSTRAINT warehouse_pkey PRIMARY KEY (warehouse_id);


--
-- Name: idx_appointments_status_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_appointments_status_date ON public.appointments USING btree (status, appointment_date);


--
-- Name: idx_appointments_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_appointments_type ON public.appointments USING btree (appointment_type);


--
-- Name: idx_appointments_variant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_appointments_variant ON public.appointments USING btree (variant_id);


--
-- Name: idx_appointments_variant_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_appointments_variant_id ON public.appointments USING btree (variant_id);


--
-- Name: idx_appointments_variant_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_appointments_variant_type ON public.appointments USING btree (variant_id, appointment_type);


--
-- Name: idx_customer_payments_order_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customer_payments_order_id ON public.customer_payments USING btree (order_id);


--
-- Name: idx_customer_payments_payment_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customer_payments_payment_date ON public.customer_payments USING btree (payment_date);


--
-- Name: idx_customer_payments_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customer_payments_status ON public.customer_payments USING btree (status);


--
-- Name: idx_customers_created_at; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customers_created_at ON public.customers USING btree (created_at);


--
-- Name: idx_customers_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customers_email ON public.customers USING btree (email);


--
-- Name: idx_customers_phone; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_customers_phone ON public.customers USING btree (phone);


--
-- Name: idx_dealer_contracts_contract_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_contracts_contract_status ON public.dealer_contracts USING btree (contract_status);


--
-- Name: idx_dealer_contracts_contract_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_contracts_contract_type ON public.dealer_contracts USING btree (contract_type);


--
-- Name: idx_dealer_contracts_end_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_contracts_end_date ON public.dealer_contracts USING btree (end_date);


--
-- Name: idx_dealer_contracts_start_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_contracts_start_date ON public.dealer_contracts USING btree (start_date);


--
-- Name: idx_dealer_invoices_quotation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_invoices_quotation ON public.dealer_invoices USING btree (quotation_id);


--
-- Name: idx_dealer_order_items_color; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_order_items_color ON public.dealer_order_items USING btree (color_id);


--
-- Name: idx_dealer_order_items_color_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_order_items_color_id ON public.dealer_order_items USING btree (color_id);


--
-- Name: idx_dealer_order_items_dealer_order_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_order_items_dealer_order_id ON public.dealer_order_items USING btree (dealer_order_id);


--
-- Name: idx_dealer_order_items_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_order_items_order ON public.dealer_order_items USING btree (dealer_order_id);


--
-- Name: idx_dealer_order_items_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_order_items_status ON public.dealer_order_items USING btree (status);


--
-- Name: idx_dealer_order_items_variant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_order_items_variant ON public.dealer_order_items USING btree (variant_id);


--
-- Name: idx_dealer_order_items_variant_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_order_items_variant_id ON public.dealer_order_items USING btree (variant_id);


--
-- Name: idx_dealer_orders_approval_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_approval_status ON public.dealer_orders USING btree (approval_status);


--
-- Name: idx_dealer_orders_approved_by; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_approved_by ON public.dealer_orders USING btree (approved_by);


--
-- Name: idx_dealer_orders_dealer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_dealer ON public.dealer_orders USING btree (dealer_id);


--
-- Name: idx_dealer_orders_dealer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_dealer_id ON public.dealer_orders USING btree (dealer_id);


--
-- Name: idx_dealer_orders_order_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_order_date ON public.dealer_orders USING btree (order_date);


--
-- Name: idx_dealer_orders_order_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_order_type ON public.dealer_orders USING btree (order_type);


--
-- Name: idx_dealer_orders_staff; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_staff ON public.dealer_orders USING btree (evm_staff_id);


--
-- Name: idx_dealer_orders_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_orders_status ON public.dealer_orders USING btree (status);


--
-- Name: idx_dealer_payments_invoice_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_payments_invoice_id ON public.dealer_payments USING btree (invoice_id);


--
-- Name: idx_dealer_quotation_items_color; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotation_items_color ON public.dealer_quotation_items USING btree (color_id);


--
-- Name: idx_dealer_quotation_items_quotation; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotation_items_quotation ON public.dealer_quotation_items USING btree (quotation_id);


--
-- Name: idx_dealer_quotation_items_variant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotation_items_variant ON public.dealer_quotation_items USING btree (variant_id);


--
-- Name: idx_dealer_quotations_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotations_date ON public.dealer_quotations USING btree (quotation_date);


--
-- Name: idx_dealer_quotations_dealer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotations_dealer ON public.dealer_quotations USING btree (dealer_id);


--
-- Name: idx_dealer_quotations_evm_staff; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotations_evm_staff ON public.dealer_quotations USING btree (evm_staff_id);


--
-- Name: idx_dealer_quotations_expiry_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotations_expiry_date ON public.dealer_quotations USING btree (expiry_date);


--
-- Name: idx_dealer_quotations_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotations_order ON public.dealer_quotations USING btree (dealer_order_id);


--
-- Name: idx_dealer_quotations_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_quotations_status ON public.dealer_quotations USING btree (status);


--
-- Name: idx_dealer_targets_dealer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_dealer ON public.dealer_targets USING btree (dealer_id);


--
-- Name: idx_dealer_targets_dealer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_dealer_id ON public.dealer_targets USING btree (dealer_id);


--
-- Name: idx_dealer_targets_dealer_scope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_dealer_scope ON public.dealer_targets USING btree (dealer_id, target_scope);


--
-- Name: idx_dealer_targets_dealer_year; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_dealer_year ON public.dealer_targets USING btree (dealer_id, target_year);


--
-- Name: idx_dealer_targets_target_month; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_target_month ON public.dealer_targets USING btree (target_month);


--
-- Name: idx_dealer_targets_target_scope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_target_scope ON public.dealer_targets USING btree (target_scope);


--
-- Name: idx_dealer_targets_target_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_target_status ON public.dealer_targets USING btree (target_status);


--
-- Name: idx_dealer_targets_target_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_target_type ON public.dealer_targets USING btree (target_type);


--
-- Name: idx_dealer_targets_target_year; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealer_targets_target_year ON public.dealer_targets USING btree (target_year);


--
-- Name: idx_dealers_city; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealers_city ON public.dealers USING btree (city);


--
-- Name: idx_dealers_province; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealers_province ON public.dealers USING btree (province);


--
-- Name: idx_dealers_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dealers_status ON public.dealers USING btree (status);


--
-- Name: idx_installment_plans_customer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_customer_id ON public.installment_plans USING btree (customer_id);


--
-- Name: idx_installment_plans_dealer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_dealer ON public.installment_plans USING btree (dealer_id);


--
-- Name: idx_installment_plans_dealer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_dealer_id ON public.installment_plans USING btree (dealer_id);


--
-- Name: idx_installment_plans_dealer_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_dealer_type ON public.installment_plans USING btree (dealer_id, plan_type);


--
-- Name: idx_installment_plans_invoice; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_invoice ON public.installment_plans USING btree (invoice_id);


--
-- Name: idx_installment_plans_invoice_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_invoice_id ON public.installment_plans USING btree (invoice_id);


--
-- Name: idx_installment_plans_order_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_order_id ON public.installment_plans USING btree (order_id);


--
-- Name: idx_installment_plans_plan_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_plan_type ON public.installment_plans USING btree (plan_type);


--
-- Name: idx_installment_plans_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_status ON public.installment_plans USING btree (plan_status);


--
-- Name: idx_installment_plans_status_created; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_status_created ON public.installment_plans USING btree (plan_status, created_at);


--
-- Name: idx_installment_plans_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_plans_type ON public.installment_plans USING btree (plan_type);


--
-- Name: idx_installment_schedules_due_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_schedules_due_date ON public.installment_schedules USING btree (due_date);


--
-- Name: idx_installment_schedules_plan_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_schedules_plan_id ON public.installment_schedules USING btree (plan_id);


--
-- Name: idx_installment_schedules_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_installment_schedules_status ON public.installment_schedules USING btree (status);


--
-- Name: idx_orders_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_customer ON public.orders USING btree (customer_id);


--
-- Name: idx_orders_customer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_customer_id ON public.orders USING btree (customer_id);


--
-- Name: idx_orders_delivery_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_delivery_status ON public.orders USING btree (delivery_status);


--
-- Name: idx_orders_inventory; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_inventory ON public.orders USING btree (inventory_id);


--
-- Name: idx_orders_order_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_order_date ON public.orders USING btree (order_date);


--
-- Name: idx_orders_order_number; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_order_number ON public.orders USING btree (order_number);


--
-- Name: idx_orders_order_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_order_type ON public.orders USING btree (order_type);


--
-- Name: idx_orders_payment_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_payment_status ON public.orders USING btree (payment_status);


--
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- Name: idx_orders_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_orders_user ON public.orders USING btree (user_id);


--
-- Name: idx_pricing_policies_dealer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pricing_policies_dealer ON public.pricing_policies USING btree (dealer_id);


--
-- Name: idx_pricing_policies_dealer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pricing_policies_dealer_id ON public.pricing_policies USING btree (dealer_id);


--
-- Name: idx_pricing_policies_dealer_scope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pricing_policies_dealer_scope ON public.pricing_policies USING btree (dealer_id, scope);


--
-- Name: idx_pricing_policies_scope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pricing_policies_scope ON public.pricing_policies USING btree (scope);


--
-- Name: idx_pricing_policies_status_effective; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pricing_policies_status_effective ON public.pricing_policies USING btree (status, effective_date);


--
-- Name: idx_pricing_policies_variant_scope; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_pricing_policies_variant_scope ON public.pricing_policies USING btree (variant_id, scope);


--
-- Name: idx_quotations_color; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quotations_color ON public.quotations USING btree (color_id);


--
-- Name: idx_quotations_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quotations_customer ON public.quotations USING btree (customer_id);


--
-- Name: idx_quotations_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quotations_user ON public.quotations USING btree (user_id);


--
-- Name: idx_quotations_variant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_quotations_variant ON public.quotations USING btree (variant_id);


--
-- Name: idx_sales_contracts_contract_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_contract_date ON public.sales_contracts USING btree (contract_date);


--
-- Name: idx_sales_contracts_contract_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_contract_status ON public.sales_contracts USING btree (contract_status);


--
-- Name: idx_sales_contracts_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_customer ON public.sales_contracts USING btree (customer_id);


--
-- Name: idx_sales_contracts_customer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_customer_id ON public.sales_contracts USING btree (customer_id);


--
-- Name: idx_sales_contracts_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_order ON public.sales_contracts USING btree (order_id);


--
-- Name: idx_sales_contracts_order_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_order_id ON public.sales_contracts USING btree (order_id);


--
-- Name: idx_sales_contracts_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_status ON public.sales_contracts USING btree (contract_status);


--
-- Name: idx_sales_contracts_user; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sales_contracts_user ON public.sales_contracts USING btree (user_id);


--
-- Name: idx_users_dealer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_dealer ON public.users USING btree (dealer_id);


--
-- Name: idx_users_dealer_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_dealer_active ON public.users USING btree (dealer_id, is_active);


--
-- Name: idx_users_dealer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_dealer_id ON public.users USING btree (dealer_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: idx_users_is_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_is_active ON public.users USING btree (is_active);


--
-- Name: idx_users_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_status ON public.users USING btree (status);


--
-- Name: idx_users_user_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_users_user_type ON public.users USING btree (user_type);


--
-- Name: idx_vehicle_deliveries_customer_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_deliveries_customer_id ON public.vehicle_deliveries USING btree (customer_id);


--
-- Name: idx_vehicle_deliveries_delivery_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_deliveries_delivery_date ON public.vehicle_deliveries USING btree (delivery_date);


--
-- Name: idx_vehicle_deliveries_delivery_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_deliveries_delivery_status ON public.vehicle_deliveries USING btree (delivery_status);


--
-- Name: idx_vehicle_deliveries_order_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_deliveries_order_id ON public.vehicle_deliveries USING btree (order_id);


--
-- Name: idx_vehicle_delivery_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_customer ON public.vehicle_deliveries USING btree (customer_id);


--
-- Name: idx_vehicle_delivery_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_date ON public.vehicle_deliveries USING btree (delivery_date);


--
-- Name: idx_vehicle_delivery_dealer_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_dealer_order ON public.vehicle_deliveries USING btree (dealer_order_id);


--
-- Name: idx_vehicle_delivery_delivered_by; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_delivered_by ON public.vehicle_deliveries USING btree (delivered_by);


--
-- Name: idx_vehicle_delivery_inventory; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_inventory ON public.vehicle_deliveries USING btree (inventory_id);


--
-- Name: idx_vehicle_delivery_order; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_order ON public.vehicle_deliveries USING btree (order_id);


--
-- Name: idx_vehicle_delivery_order_item; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_order_item ON public.vehicle_deliveries USING btree (dealer_order_item_id);


--
-- Name: idx_vehicle_delivery_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_delivery_status ON public.vehicle_deliveries USING btree (delivery_status);


--
-- Name: idx_vehicle_inventory_color; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_color ON public.vehicle_inventory USING btree (color_id);


--
-- Name: idx_vehicle_inventory_condition; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_condition ON public.vehicle_inventory USING btree (condition);


--
-- Name: idx_vehicle_inventory_reserved; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_reserved ON public.vehicle_inventory USING btree (reserved_for_dealer);


--
-- Name: idx_vehicle_inventory_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_status ON public.vehicle_inventory USING btree (status);


--
-- Name: idx_vehicle_inventory_variant; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_variant ON public.vehicle_inventory USING btree (variant_id);


--
-- Name: idx_vehicle_inventory_variant_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_variant_id ON public.vehicle_inventory USING btree (variant_id);


--
-- Name: idx_vehicle_inventory_vin; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_vin ON public.vehicle_inventory USING btree (vin);


--
-- Name: idx_vehicle_inventory_warehouse; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_inventory_warehouse ON public.vehicle_inventory USING btree (warehouse_id);


--
-- Name: idx_vehicle_models_brand_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_models_brand_id ON public.vehicle_models USING btree (brand_id);


--
-- Name: idx_vehicle_variants_model_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_vehicle_variants_model_id ON public.vehicle_variants USING btree (model_id);


--
-- Name: dealer_order_items trigger_update_dealer_order_items_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_dealer_order_items_updated_at BEFORE UPDATE ON public.dealer_order_items FOR EACH ROW EXECUTE FUNCTION public.update_dealer_order_items_updated_at();


--
-- Name: dealer_quotations trigger_update_dealer_quotation_expiry_date; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_dealer_quotation_expiry_date BEFORE INSERT OR UPDATE OF quotation_date, validity_days ON public.dealer_quotations FOR EACH ROW EXECUTE FUNCTION public.update_dealer_quotation_expiry_date();


--
-- Name: dealer_quotation_items trigger_update_dealer_quotation_item_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_dealer_quotation_item_updated_at BEFORE UPDATE ON public.dealer_quotation_items FOR EACH ROW EXECUTE FUNCTION public.update_dealer_quotation_updated_at();


--
-- Name: dealer_quotations trigger_update_dealer_quotation_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_dealer_quotation_updated_at BEFORE UPDATE ON public.dealer_quotations FOR EACH ROW EXECUTE FUNCTION public.update_dealer_quotation_updated_at();


--
-- Name: customer_feedbacks update_customer_feedbacks_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_customer_feedbacks_updated_at BEFORE UPDATE ON public.customer_feedbacks FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: customers update_customers_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON public.customers FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: dealer_contracts update_dealer_contracts_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_dealer_contracts_updated_at BEFORE UPDATE ON public.dealer_contracts FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: dealer_invoices update_dealer_invoices_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_dealer_invoices_updated_at BEFORE UPDATE ON public.dealer_invoices FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: dealer_orders update_dealer_orders_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_dealer_orders_updated_at BEFORE UPDATE ON public.dealer_orders FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: dealer_targets update_dealer_targets_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_dealer_targets_updated_at BEFORE UPDATE ON public.dealer_targets FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: dealers update_dealers_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_dealers_updated_at BEFORE UPDATE ON public.dealers FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: orders update_orders_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON public.orders FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: promotions update_promotions_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_promotions_updated_at BEFORE UPDATE ON public.promotions FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: quotations update_quotations_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_quotations_updated_at BEFORE UPDATE ON public.quotations FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: sales_contracts update_sales_contracts_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_sales_contracts_updated_at BEFORE UPDATE ON public.sales_contracts FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: users update_users_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: vehicle_deliveries update_vehicle_deliveries_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_vehicle_deliveries_updated_at BEFORE UPDATE ON public.vehicle_deliveries FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: vehicle_inventory update_vehicle_inventory_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_vehicle_inventory_updated_at BEFORE UPDATE ON public.vehicle_inventory FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();


--
-- Name: customer_feedbacks customer_feedbacks_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_feedbacks
    ADD CONSTRAINT customer_feedbacks_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: customer_feedbacks customer_feedbacks_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_feedbacks
    ADD CONSTRAINT customer_feedbacks_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);


--
-- Name: customer_payments customer_payments_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_payments
    ADD CONSTRAINT customer_payments_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: customer_payments customer_payments_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_payments
    ADD CONSTRAINT customer_payments_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);


--
-- Name: customer_payments customer_payments_processed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_payments
    ADD CONSTRAINT customer_payments_processed_by_fkey FOREIGN KEY (processed_by) REFERENCES public.users(user_id);


--
-- Name: dealer_contracts dealer_contracts_dealer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_contracts
    ADD CONSTRAINT dealer_contracts_dealer_id_fkey FOREIGN KEY (dealer_id) REFERENCES public.dealers(dealer_id);


--
-- Name: dealer_invoices dealer_invoices_dealer_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_invoices
    ADD CONSTRAINT dealer_invoices_dealer_order_id_fkey FOREIGN KEY (dealer_order_id) REFERENCES public.dealer_orders(dealer_order_id);


--
-- Name: dealer_invoices dealer_invoices_evm_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_invoices
    ADD CONSTRAINT dealer_invoices_evm_staff_id_fkey FOREIGN KEY (evm_staff_id) REFERENCES public.users(user_id);


--
-- Name: dealer_orders dealer_orders_evm_staff_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_orders
    ADD CONSTRAINT dealer_orders_evm_staff_id_fkey FOREIGN KEY (evm_staff_id) REFERENCES public.users(user_id);


--
-- Name: dealer_payments dealer_payments_invoice_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_payments
    ADD CONSTRAINT dealer_payments_invoice_id_fkey FOREIGN KEY (invoice_id) REFERENCES public.dealer_invoices(invoice_id);


--
-- Name: dealer_order_items fk1v25oc0yd3idvdybnnrohvy9y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_order_items
    ADD CONSTRAINT fk1v25oc0yd3idvdybnnrohvy9y FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: appointments fk88083ngr9rv9wj4p916pj40c2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fk88083ngr9rv9wj4p916pj40c2 FOREIGN KEY (staff_id) REFERENCES public.users(user_id);


--
-- Name: dealer_installment_plans fk92d3eqekwmmm7fnj4a05whbc5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_installment_plans
    ADD CONSTRAINT fk92d3eqekwmmm7fnj4a05whbc5 FOREIGN KEY (invoice_id) REFERENCES public.dealer_invoices(invoice_id);


--
-- Name: customer_invoices fk94vmdod2h1gvsuam7u7u22xb4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_invoices
    ADD CONSTRAINT fk94vmdod2h1gvsuam7u7u22xb4 FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: dealer_discount_policies fk9k40cr9dwn2jdhhjduf4hness; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_discount_policies
    ADD CONSTRAINT fk9k40cr9dwn2jdhhjduf4hness FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: appointments fk_appointments_variant; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fk_appointments_variant FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: dealer_invoices fk_dealer_invoices_quotation; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_invoices
    ADD CONSTRAINT fk_dealer_invoices_quotation FOREIGN KEY (quotation_id) REFERENCES public.dealer_quotations(quotation_id) ON DELETE SET NULL;


--
-- Name: dealer_orders fk_dealer_orders_approved_by; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_orders
    ADD CONSTRAINT fk_dealer_orders_approved_by FOREIGN KEY (approved_by) REFERENCES public.users(user_id) ON DELETE SET NULL;


--
-- Name: dealer_orders fk_dealer_orders_dealer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_orders
    ADD CONSTRAINT fk_dealer_orders_dealer FOREIGN KEY (dealer_id) REFERENCES public.dealers(dealer_id) ON DELETE RESTRICT;


--
-- Name: dealer_quotation_items fk_dealer_quotation_items_color; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotation_items
    ADD CONSTRAINT fk_dealer_quotation_items_color FOREIGN KEY (color_id) REFERENCES public.vehicle_colors(color_id) ON DELETE RESTRICT;


--
-- Name: dealer_quotation_items fk_dealer_quotation_items_quotation; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotation_items
    ADD CONSTRAINT fk_dealer_quotation_items_quotation FOREIGN KEY (quotation_id) REFERENCES public.dealer_quotations(quotation_id) ON DELETE CASCADE;


--
-- Name: dealer_quotation_items fk_dealer_quotation_items_variant; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotation_items
    ADD CONSTRAINT fk_dealer_quotation_items_variant FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id) ON DELETE RESTRICT;


--
-- Name: dealer_quotations fk_dealer_quotations_dealer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotations
    ADD CONSTRAINT fk_dealer_quotations_dealer FOREIGN KEY (dealer_id) REFERENCES public.dealers(dealer_id) ON DELETE RESTRICT;


--
-- Name: dealer_quotations fk_dealer_quotations_dealer_order; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotations
    ADD CONSTRAINT fk_dealer_quotations_dealer_order FOREIGN KEY (dealer_order_id) REFERENCES public.dealer_orders(dealer_order_id) ON DELETE SET NULL;


--
-- Name: dealer_quotations fk_dealer_quotations_evm_staff; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_quotations
    ADD CONSTRAINT fk_dealer_quotations_evm_staff FOREIGN KEY (evm_staff_id) REFERENCES public.users(user_id) ON DELETE SET NULL;


--
-- Name: dealer_targets fk_dealer_targets_dealer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_targets
    ADD CONSTRAINT fk_dealer_targets_dealer FOREIGN KEY (dealer_id) REFERENCES public.dealers(dealer_id);


--
-- Name: installment_plans fk_installment_plans_dealer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.installment_plans
    ADD CONSTRAINT fk_installment_plans_dealer FOREIGN KEY (dealer_id) REFERENCES public.dealers(dealer_id);


--
-- Name: installment_plans fk_installment_plans_invoice; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.installment_plans
    ADD CONSTRAINT fk_installment_plans_invoice FOREIGN KEY (invoice_id) REFERENCES public.dealer_invoices(invoice_id);


--
-- Name: pricing_policies fk_pricing_policies_dealer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pricing_policies
    ADD CONSTRAINT fk_pricing_policies_dealer FOREIGN KEY (dealer_id) REFERENCES public.dealers(dealer_id);


--
-- Name: users fk_users_dealer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_users_dealer FOREIGN KEY (dealer_id) REFERENCES public.dealers(dealer_id);


--
-- Name: dealer_order_items fka45g8x4aalb5jy5m5d6ty17ae; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_order_items
    ADD CONSTRAINT fka45g8x4aalb5jy5m5d6ty17ae FOREIGN KEY (dealer_order_id) REFERENCES public.dealer_orders(dealer_order_id);


--
-- Name: pricing_policies fkd7eqnxkd9h9us7tj6c1q89jny; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pricing_policies
    ADD CONSTRAINT fkd7eqnxkd9h9us7tj6c1q89jny FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: test_drive_schedules fkdgr7gp15xmlvd7adomimmw2wt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_drive_schedules
    ADD CONSTRAINT fkdgr7gp15xmlvd7adomimmw2wt FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: dealer_installment_schedules fkfr6c0gs94glrnuq9b3smnbox0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_installment_schedules
    ADD CONSTRAINT fkfr6c0gs94glrnuq9b3smnbox0 FOREIGN KEY (plan_id) REFERENCES public.dealer_installment_plans(plan_id);


--
-- Name: customer_invoices fkgik7mb6habfxb6vixyyo6mqtc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_invoices
    ADD CONSTRAINT fkgik7mb6habfxb6vixyyo6mqtc FOREIGN KEY (order_id) REFERENCES public.orders(order_id);


--
-- Name: vehicle_deliveries fkk7d4fky5l0dayx3ds3uwv2tcf; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_deliveries
    ADD CONSTRAINT fkk7d4fky5l0dayx3ds3uwv2tcf FOREIGN KEY (dealer_order_id) REFERENCES public.dealer_orders(dealer_order_id);


--
-- Name: dealer_order_items fkl7qanqkf30dd24bibtcti14xp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dealer_order_items
    ADD CONSTRAINT fkl7qanqkf30dd24bibtcti14xp FOREIGN KEY (color_id) REFERENCES public.vehicle_colors(color_id);


--
-- Name: test_drive_schedules fkn1dd9vk9aqi236u4s06n5iant; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.test_drive_schedules
    ADD CONSTRAINT fkn1dd9vk9aqi236u4s06n5iant FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: vehicle_deliveries fkrk2p6pe3wl2b533ncwj9ac8d0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_deliveries
    ADD CONSTRAINT fkrk2p6pe3wl2b533ncwj9ac8d0 FOREIGN KEY (dealer_order_item_id) REFERENCES public.dealer_order_items(item_id);


--
-- Name: appointments fkrlbb09f329sfsmftrh7y0yxtk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointments
    ADD CONSTRAINT fkrlbb09f329sfsmftrh7y0yxtk FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: customer_invoices fkt0xgkh5fn1q8hqndamdq286dh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer_invoices
    ADD CONSTRAINT fkt0xgkh5fn1q8hqndamdq286dh FOREIGN KEY (evm_staff_id) REFERENCES public.users(user_id);


--
-- Name: installment_plans installment_plans_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.installment_plans
    ADD CONSTRAINT installment_plans_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: installment_plans installment_plans_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.installment_plans
    ADD CONSTRAINT installment_plans_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);


--
-- Name: installment_schedules installment_schedules_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.installment_schedules
    ADD CONSTRAINT installment_schedules_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.installment_plans(plan_id);


--
-- Name: orders orders_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: orders orders_inventory_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_inventory_id_fkey FOREIGN KEY (inventory_id) REFERENCES public.vehicle_inventory(inventory_id);


--
-- Name: orders orders_quotation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_quotation_id_fkey FOREIGN KEY (quotation_id) REFERENCES public.quotations(quotation_id);


--
-- Name: orders orders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: promotions promotions_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotions
    ADD CONSTRAINT promotions_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: quotations quotations_color_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quotations
    ADD CONSTRAINT quotations_color_id_fkey FOREIGN KEY (color_id) REFERENCES public.vehicle_colors(color_id);


--
-- Name: quotations quotations_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quotations
    ADD CONSTRAINT quotations_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: quotations quotations_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quotations
    ADD CONSTRAINT quotations_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: quotations quotations_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.quotations
    ADD CONSTRAINT quotations_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: sales_contracts sales_contracts_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_contracts
    ADD CONSTRAINT sales_contracts_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: sales_contracts sales_contracts_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_contracts
    ADD CONSTRAINT sales_contracts_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);


--
-- Name: sales_contracts sales_contracts_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sales_contracts
    ADD CONSTRAINT sales_contracts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: vehicle_deliveries vehicle_deliveries_customer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_deliveries
    ADD CONSTRAINT vehicle_deliveries_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES public.customers(customer_id);


--
-- Name: vehicle_deliveries vehicle_deliveries_delivered_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_deliveries
    ADD CONSTRAINT vehicle_deliveries_delivered_by_fkey FOREIGN KEY (delivered_by) REFERENCES public.users(user_id);


--
-- Name: vehicle_deliveries vehicle_deliveries_inventory_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_deliveries
    ADD CONSTRAINT vehicle_deliveries_inventory_id_fkey FOREIGN KEY (inventory_id) REFERENCES public.vehicle_inventory(inventory_id);


--
-- Name: vehicle_deliveries vehicle_deliveries_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_deliveries
    ADD CONSTRAINT vehicle_deliveries_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);


--
-- Name: vehicle_inventory vehicle_inventory_color_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_inventory
    ADD CONSTRAINT vehicle_inventory_color_id_fkey FOREIGN KEY (color_id) REFERENCES public.vehicle_colors(color_id);


--
-- Name: vehicle_inventory vehicle_inventory_reserved_for_customer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_inventory
    ADD CONSTRAINT vehicle_inventory_reserved_for_customer_fkey FOREIGN KEY (reserved_for_customer) REFERENCES public.customers(customer_id);


--
-- Name: vehicle_inventory vehicle_inventory_reserved_for_dealer_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_inventory
    ADD CONSTRAINT vehicle_inventory_reserved_for_dealer_fkey FOREIGN KEY (reserved_for_dealer) REFERENCES public.dealers(dealer_id);


--
-- Name: vehicle_inventory vehicle_inventory_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_inventory
    ADD CONSTRAINT vehicle_inventory_variant_id_fkey FOREIGN KEY (variant_id) REFERENCES public.vehicle_variants(variant_id);


--
-- Name: vehicle_inventory vehicle_inventory_warehouse_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_inventory
    ADD CONSTRAINT vehicle_inventory_warehouse_id_fkey FOREIGN KEY (warehouse_id) REFERENCES public.warehouse(warehouse_id);


--
-- Name: vehicle_models vehicle_models_brand_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_models
    ADD CONSTRAINT vehicle_models_brand_id_fkey FOREIGN KEY (brand_id) REFERENCES public.vehicle_brands(brand_id);


--
-- Name: vehicle_variants vehicle_variants_model_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicle_variants
    ADD CONSTRAINT vehicle_variants_model_id_fkey FOREIGN KEY (model_id) REFERENCES public.vehicle_models(model_id);


--
-- PostgreSQL database dump complete
--

\unrestrict lZCilG6Hfx4j7dEPpRmT3S2SwE6KHG0JOXHYvMTJspDbkby6b4lDwTT1VxoOgSQ

