CREATE TYPE valid_skill_levels AS ENUM ('beginner', 'intermediate', 'advanced');
CREATE TYPE valid_lesson_types AS ENUM ('individual_lesson', 'group_lesson', 'ensemble');
CREATE TYPE valid_instrument_type AS ENUM ('Guitar', 'Violin', 'Drums', 'Saxophone', 'Flute', 'Ukulele', 'Trumpet', 'Lyra', 'Viola', 'Piano');

CREATE TABLE instructor (
 instructor_id SERIAL NOT NULL,
 person_number VARCHAR(12) NOT NULL UNIQUE,
 name VARCHAR(200) NOT NULL,
 street VARCHAR(200) NOT NULL,
 zip VARCHAR(10) NOT NULL,
 city VARCHAR(200) NOT NULL,
 teach_ensembles BIT(1) NOT NULL,
 email VARCHAR(200) NOT NULL,
 phone VARCHAR(200) NOT NULL
);

ALTER TABLE instructor ADD CONSTRAINT PK_instructor PRIMARY KEY (instructor_id);


CREATE TABLE instrument (
    instrument_id SERIAL NOT NULL,
    type valid_instrument_type NOT NULL,
    brand VARCHAR(200),
    price INT NOT NULL
);

ALTER TABLE instrument ADD CONSTRAINT PK_instrument PRIMARY KEY (instrument_id);


CREATE TABLE pricing_scheme (
 pricing_scheme_id SERIAL NOT NULL,
 skill_level valid_skill_levels,
 lesson_type valid_lesson_types NOT NULL,
 instructor_cut INT NOT NULL,
 student_pay INT NOT NULL,
 discount INT
);

ALTER TABLE pricing_scheme ADD CONSTRAINT PK_pricing_scheme PRIMARY KEY (pricing_scheme_id);


CREATE TABLE student (
 student_id SERIAL NOT NULL,
 person_number VARCHAR(12) NOT NULL UNIQUE,
 name VARCHAR(100) NOT NULL,
 street VARCHAR(100) NOT NULL,
 zip VARCHAR(5) NOT NULL,
 city VARCHAR(100) NOT NULL
);

ALTER TABLE student ADD CONSTRAINT PK_student PRIMARY KEY (student_id);


CREATE TABLE available_at (
 instructor_id SERIAL NOT NULL,
 time TIMESTAMP(6) NOT NULL
);

ALTER TABLE available_at ADD CONSTRAINT PK_available_at PRIMARY KEY (instructor_id,time);


CREATE TABLE can_teach (
 instructor_id SERIAL NOT NULL,
 instrument_type valid_instrument_type NOT NULL
);

ALTER TABLE can_teach ADD CONSTRAINT PK_can_teach PRIMARY KEY (instructor_id, instrument_type);


CREATE TABLE contact_person (
 student_id SERIAL NOT NULL,
 name VARCHAR(100),
 phone VARCHAR(200) NOT NULL,
 email VARCHAR(200) NOT NULL
);

ALTER TABLE contact_person ADD CONSTRAINT PK_contact_person PRIMARY KEY (student_id);


CREATE TABLE email (
 student_id SERIAL NOT NULL,
 email VARCHAR(200) NOT NULL
);

ALTER TABLE email ADD CONSTRAINT PK_email PRIMARY KEY (student_id,email);


CREATE TABLE lesson (
 lesson_id SERIAL NOT NULL,
 skill_level valid_skill_levels,
 lesson_type valid_lesson_types NOT NULL,
 time TIMESTAMP(6) NOT NULL,
 pricing_scheme_id SERIAL,
 instructor_id SERIAL,
 genre VARCHAR(200),
 instrument_type valid_instrument_type,
 min_students INT,
 max_students INT,
 student_amount INT
);

ALTER TABLE lesson ADD CONSTRAINT PK_lesson PRIMARY KEY (lesson_id);


CREATE TABLE phone (
 student_id SERIAL NOT NULL,
 phone VARCHAR(200) NOT NULL
);

ALTER TABLE phone ADD CONSTRAINT PK_phone PRIMARY KEY (student_id,phone);


CREATE TABLE rental (
 rental_id SERIAL NOT NULL,
 student_id SERIAL NOT NULL,
 time_rented TIMESTAMP(6) NOT NULL,
 rental_terminated BIT(1) NOT NULL,
 instrument_id SERIAL
);

ALTER TABLE rental ADD CONSTRAINT PK_rental PRIMARY KEY (rental_id);


CREATE TABLE sibling (
    student_id INT NOT NULL,
    sibling_student_id INT NOT NULL
);

ALTER TABLE sibling ADD CONSTRAINT PK_sibling PRIMARY KEY (student_id,sibling_student_id);


CREATE TABLE student_lesson (
 student_id SERIAL NOT NULL,
 lesson_id SERIAL NOT NULL
);

ALTER TABLE student_lesson ADD CONSTRAINT PK_student_lesson PRIMARY KEY (student_id,lesson_id);

ALTER TABLE available_at ADD CONSTRAINT FK_available_at_0 FOREIGN KEY (instructor_id) REFERENCES instructor (instructor_id) ON DELETE CASCADE;

ALTER TABLE can_teach ADD CONSTRAINT FK_can_teach_0 FOREIGN KEY (instructor_id) REFERENCES instructor (instructor_id) ON DELETE CASCADE;

ALTER TABLE contact_person ADD CONSTRAINT FK_contact_person_0 FOREIGN KEY (student_id) REFERENCES student (student_id) ON DELETE CASCADE;

ALTER TABLE email ADD CONSTRAINT FK_email_0 FOREIGN KEY (student_id) REFERENCES student (student_id) ON DELETE CASCADE;

ALTER TABLE lesson ADD CONSTRAINT FK_lesson_0 FOREIGN KEY (pricing_scheme_id) REFERENCES pricing_scheme (pricing_scheme_id) ON DELETE SET NULL;
ALTER TABLE lesson ADD CONSTRAINT FK_lesson_1 FOREIGN KEY (instructor_id) REFERENCES instructor (instructor_id) ON DELETE SET NULL;

ALTER TABLE phone ADD CONSTRAINT FK_phone_0 FOREIGN KEY (student_id) REFERENCES student (student_id) ON DELETE CASCADE;

ALTER TABLE sibling ADD CONSTRAINT FK_sibling_0 FOREIGN KEY (student_id) REFERENCES student (student_id) ON DELETE CASCADE;
ALTER TABLE sibling ADD CONSTRAINT FK_sibling_1 FOREIGN KEY (sibling_student_id) REFERENCES student (student_id) ON DELETE CASCADE;

ALTER TABLE rental ADD CONSTRAINT FK_rental_0 FOREIGN KEY (student_id) REFERENCES student (student_id) ON DELETE CASCADE;
ALTER TABLE rental ADD CONSTRAINT FK_rental_1 FOREIGN KEY (instrument_id) REFERENCES instrument (instrument_id) ON DELETE SET NULL;

ALTER TABLE student_lesson ADD CONSTRAINT FK_student_lesson_0 FOREIGN KEY (student_id) REFERENCES lesson (lesson_id) ON DELETE CASCADE;
ALTER TABLE student_lesson ADD CONSTRAINT FK_student_lesson_1 FOREIGN KEY (lesson_id) REFERENCES student (student_id) ON DELETE CASCADE;