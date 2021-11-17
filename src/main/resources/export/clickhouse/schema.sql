CREATE TABLE IF NOT EXISTS template.variables
(
    ts DateTime,
    id String,
    long_name_1 Nullable(String),
    long_id_1 Nullable(UUID),
    long_val_1 Nullable(Int64),
    long_name_2 Nullable(String),
    long_id_2 Nullable(UUID),
    long_val_2 Nullable(Int64),
    long_name_3 Nullable(String),
    long_id_3 Nullable(UUID),
    long_val_3 Nullable(Int64),
    string_name_1 Nullable(String),
    string_id_1 Nullable(UUID),
    string_val_1 Nullable(String),
    string_name_2 Nullable(String),
    string_id_2 Nullable(UUID),
    string_val_2 Nullable(String),
    string_name_3 Nullable(String),
    string_id_3 Nullable(UUID),
    string_val_3 Nullable(String)
) ENGINE = MergeTree()
ORDER BY (ts, id)
PRIMARY KEY (ts, id)
PARTITION BY (toDate(ts))
