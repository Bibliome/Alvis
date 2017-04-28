--
--
set search_path to XXXXX;

--drop index "campaigndocumentIDX1";
--alter table campaigndocument drop column alvisnlp_id;


alter table campaigndocument add column alvisnlp_id varchar(128);

create unique index "campaigndocumentIDX1" on campaigndocument(alvisnlp_id);
