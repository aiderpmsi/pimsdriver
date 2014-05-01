CREATE SCHEMA pmel;

CREATE TYPE public.plud_status AS ENUM ('pending', 'successed', 'failed');

CREATE TABLE public.plud_pmsiupload (
  plud_id bigserial NOT NULL,
  plud_processed plud_status NOT NULL,
  plud_finess character varying NOT NULL,
  plud_year integer NOT NULL,
  plud_month smallint NOT NULL,
  plud_dateenvoi timestamp with time zone NOT NULL,
  plud_rsf_oid oid,
  plud_rss_oid oid,
  plud_arguments hstore,
  CONSTRAINT plud_pmsiupload_pkey PRIMARY KEY (plud_id)
);

CREATE TABLE public.pmel_pmsielement (
  pmel_id bigserial NOT NULL,
  pmel_root bigint NOT NULL,
  pmel_parent bigint,
  pmel_type character varying NOT NULL,
  pmel_attributes hstore NOT NULL
);