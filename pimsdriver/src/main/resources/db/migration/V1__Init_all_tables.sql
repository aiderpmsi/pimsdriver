CREATE TYPE plud_status AS ENUM ('pending', 'successed', 'failed');

CREATE TABLE plud_pmsiupload (
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

CREATE TABLE pmel_pmsielement (
  pmel_id bigserial NOT NULL,
  pmel_root bigint NOT NULL,
  pmel_parent bigint,
  pmel_type character varying NOT NULL,
  pmel_attributes hstore NOT NULL,
  CONSTRAINT pmel_pmsielement_pkey PRIMARY KEY (pmel_id),
  CONSTRAINT pmel_pmsielement_pmel_parent_fkey FOREIGN KEY (pmel_parent)
      REFERENCES pmel_pmsielement (pmel_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED,
  CONSTRAINT pmel_pmsielement_pmel_root_fkey FOREIGN KEY (pmel_root)
      REFERENCES plud_pmsiupload (plud_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY DEFERRED
);

CREATE INDEX pmel_pmsielement_pmel_root_idx ON pmel_pmsielement USING btree (pmel_root);