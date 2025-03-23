CREATE TABLE user_roles
(
    user_id UUID         NOT NULL,
    role    VARCHAR(255) NOT NULL
);

CREATE TABLE users
(
    id       UUID NOT NULL,
    username VARCHAR(255),
    password VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_on_user FOREIGN KEY (user_id) REFERENCES users (id);