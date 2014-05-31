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
  plud_arguments hstore NOT NULL DEFAULT hstore(''),
  CONSTRAINT plud_pmsiupload_pkey PRIMARY KEY (plud_id)
);

CREATE TABLE public.pmel_pmsielement (
  pmel_id bigserial NOT NULL,
  pmel_root bigint NOT NULL,
  pmel_root_type character varying NOT NULL,
  pmel_parent bigint,
  pmel_type character varying NOT NULL,
  pmel_line bigint NOT NULL,
  pmel_content character varying NOT NULL,
  pmel_arguments hstore NOT NULL DEFAULT hstore('')
);

CREATE TABLE pmel.pmel_cleanup (
  plud_id bigint NOT NULL
);

CREATE VIEW public.fhva_rsfheaders_2012_view AS
SELECT pmel_id,
       pmel_root,
       pmel_parent,
       pmel_type,
       pmel_line,
       substring(pmel_content from 1 for 10) Finess,
       substring(pmel_content from 10 for 3) NumLot,
       substring(pmel_content from 13 for 2) StatutJuridique,
       substring(pmel_content from 15 for 2) ModeTarifs,
       substring(pmel_content from 17 for 8) DateDebut,
       substring(pmel_content from 25 for 8) DateFin,
       substring(pmel_content from 33 for 6) NbEnregistrements,
       substring(pmel_content from 39 for 6) NbRSS,
       substring(pmel_content from 46 for 7) PremierRss,
       substring(pmel_content from 53 for 7) DernierRss,
       substring(pmel_content from 60 for 1) DernierEnvoi,
       pmel_arguments
  FROM public.pmel_pmsielement
  WHERE pmel_type = 'rsfheader';
 
CREATE VIEW public.fava_rsfa_2012_view AS
SELECT pmel_id,
       pmel_root,
       pmel_parent,
       pmel_type,
       pmel_line,
       'A'::character varying TypeEnregistrement,
       substring(pmel_content from 2 for 9) Finess,
       substring(pmel_content from 11 for 20) NumRSS,
       substring(pmel_content from 31 for 1) Sexe,
       substring(pmel_content from 32 for 1) CodeCivilite,
       substring(pmel_content from 33 for 13) CodeSS,
       substring(pmel_content from 46 for 2) CleCodeSS,
       substring(pmel_content from 48 for 3) RangBeneficiaire,
       substring(pmel_content from 51 for 9) NumFacture,
       substring(pmel_content from 60 for 1) NatureOperation,
       substring(pmel_content from 61 for 2) NatureAssurance,
       substring(pmel_content from 63 for 2) TypeContratOC,
       substring(pmel_content from 65 for 1) JustifExonerationTM,
       substring(pmel_content from 66 for 1) CodePEC,
       substring(pmel_content from 67 for 2) CodeGdRegime,
       substring(pmel_content from 69 for 8) DateNaissance,
       substring(pmel_content from 77 for 1) RangNaissance,
       substring(pmel_content from 78 for 8) DateEntree,
       substring(pmel_content from 86 for 8) DateSortie,
       substring(pmel_content from 94 for 8) TotalBaseRemboursementPH,
       substring(pmel_content from 102 for 8) TotalRemboursableAMOPH,
       substring(pmel_content from 110 for 8) TotalFactureHonoraire,
       substring(pmel_content from 118 for 8) TotalRemboursableAMOHonoraire,
       substring(pmel_content from 126 for 8) TotalParticipationAvantOC,
       substring(pmel_content from 134 for 8) TotalRemboursableOCPH,
       substring(pmel_content from 142 for 8) TotalRemboursableOCHonoraire,
       substring(pmel_content from 150 for 8) TotalFacturePH,
       substring(pmel_content from 158 for 1) EtatLiquidation,
       substring(pmel_content from 159 for 1) CMU,
       substring(pmel_content from 160 for 9) LienMere,
       pmel_arguments
  FROM public.pmel_pmsielement
  WHERE pmel_type = 'rsfa';

CREATE VIEW public.favb_rsfb_2012_view AS
SELECT pmel_id,
       pmel_root,
       pmel_parent,
       pmel_type,
       pmel_line,
       'B'::character varying TypeEnregistrement,
       substring(pmel_content from 2 for 9) Finess,
       substring(pmel_content from 11 for 20) NumRSS,
       substring(pmel_content from 31 for 13) CodeSS,
       substring(pmel_content from 44 for 2) CleCodeSS,
       substring(pmel_content from 46 for 3) RangBeneficiaire,
       substring(pmel_content from 49 for 9) NumFacture,
       substring(pmel_content from 58 for 2) ModeTraitement,
       substring(pmel_content from 60 for 3) DisciplinePrestation,
       substring(pmel_content from 63 for 8) DateDebutSejour,
       substring(pmel_content from 71 for 8) DateFinSejour,
       substring(pmel_content from 79 for 5) CodeActe,
       substring(pmel_content from 84 for 3) Quantite,
       substring(pmel_content from 87 for 1) JustifExonerationTM,
       substring(pmel_content from 88 for 5) Coefficient,
       substring(pmel_content from 93 for 1) CodePEC,
       substring(pmel_content from 94 for 5) CoefficientMCO,
       substring(pmel_content from 99 for 7) PrixUnitaire,
       substring(pmel_content from 106 for 8) MontantBaseRemboursementPH,
       substring(pmel_content from 114 for 3) TauxPrestation,
       substring(pmel_content from 117 for 8) MontantRemboursableAMOPH,
       substring(pmel_content from 125 for 8) MontantTotalDepense,
       substring(pmel_content from 133 for 7) MontantRemboursableOCPH,
       substring(pmel_content from 140 for 4) NumGHS,
       substring(pmel_content from 144 for 8) MontantNOEMIE,
       substring(pmel_content from 152 for 3) OperationNOEMIE,
       pmel_arguments
  FROM public.pmel_pmsielement
  WHERE pmel_type = 'rsfb';

CREATE VIEW public.favc_rsfc_2012_view AS
SELECT pmel_id,
       pmel_root,
       pmel_parent,
       pmel_type,
       pmel_line,
       'C'::character varying TypeEnregistrement,
       substring(pmel_content from 2 for 9) Finess,
       substring(pmel_content from 11 for 20) NumRSS,
       substring(pmel_content from 31 for 13) CodeSS,
       substring(pmel_content from 44 for 2) CleCodeSS,
       substring(pmel_content from 46 for 3) RangBeneficiaire,
       substring(pmel_content from 49 for 9) NumFacture,
       substring(pmel_content from 58 for 2) ModeTraitement,
       substring(pmel_content from 60 for 3) DisciplinePrestation,
       substring(pmel_content from 63 for 1) JustifExonerationTM,
       substring(pmel_content from 64 for 8) DateActe,
       substring(pmel_content from 72 for 5) CodeActe,
       substring(pmel_content from 77 for 2) Quantite,
       substring(pmel_content from 79 for 6) Coefficient,
       substring(pmel_content from 85 for 2) Denombrement,
       substring(pmel_content from 87 for 7) PrixUnitaire,
       substring(pmel_content from 94 for 7) MontantBaseRemboursementHonoraire,
       substring(pmel_content from 101 for 3) TauxRemboursement,
       substring(pmel_content from 104 for 7) MontantRemboursableAMOHonoraire,
       substring(pmel_content from 111 for 7) MontantTotalHonoraire,
       substring(pmel_content from 118 for 6) MontantRemboursableOCHonoraire,
       substring(pmel_content from 124 for 8) MontantNOEMIE,
       substring(pmel_content from 132 for 3) OperationNOEMIE,
       pmel_arguments
  FROM public.pmel_pmsielement
  WHERE pmel_type = 'rsfc';

CREATE VIEW public.favh_rsfh_2012_view AS
SELECT pmel_id,
       pmel_root,
       pmel_parent,
       pmel_type,
       pmel_line,
       'H'::character varying TypeEnregistrement,
       substring(pmel_content from 2 for 9) Finess,
       substring(pmel_content from 11 for 20) NumRSS,
       substring(pmel_content from 31 for 13) CodeSS,
       substring(pmel_content from 44 for 2) CleCodeSS,
       substring(pmel_content from 46 for 3) RangBeneficiaire,
       substring(pmel_content from 49 for 9) NumFacture,
       substring(pmel_content from 58 for 8) DateDebutSejour,
       substring(pmel_content from 66 for 7) CodeUCD,
       substring(pmel_content from 73 for 5) CoefficientFractionnement,
       substring(pmel_content from 78 for 7) PrixAchatUnitaire,
       substring(pmel_content from 85 for 7) MontantUnitaireEcartIndemnisable,
       substring(pmel_content from 92 for 7) MontantTotalEcartIndemnisable,
       substring(pmel_content from 99 for 3) Quantite,
       substring(pmel_content from 102 for 7) MontantTotalFactureTTC,
       pmel_arguments
  FROM public.pmel_pmsielement
  WHERE pmel_type = 'rsfh';
