CREATE TABLE IF NOT EXISTS template.variables
(
    ts DateTime,
    id UUID,
    long_name_1 Nullable(String),
    long_id_1 Nullable(UUID),
    long_val_1 Nullable(Int64),
    long_name_2 Nullable(String),
    long_id_2 Nullable(UUID),
    long_val_2 Nullable(Int64),
    long_name_3 Nullable(String),
    long_id_3 Nullable(UUID),
    long_val_3 Nullable(Int64),
    long_name_4 Nullable(String),
    long_id_4 Nullable(UUID),
    long_val_4 Nullable(Int64),
    long_name_5 Nullable(String),
    long_id_5 Nullable(UUID),
    long_val_5 Nullable(Int64),
    long_name_6 Nullable(String),
    long_id_6 Nullable(UUID),
    long_val_6 Nullable(Int64),
    long_name_7 Nullable(String),
    long_id_7 Nullable(UUID),
    long_val_7 Nullable(Int64),
    long_name_8 Nullable(String),
    long_id_8 Nullable(UUID),
    long_val_8 Nullable(Int64),
    long_name_9 Nullable(String),
    long_id_9 Nullable(UUID),
    long_val_9 Nullable(Int64),
    long_name_10 Nullable(String),
    long_id_10 Nullable(UUID),
    long_val_10 Nullable(Int64),
    string_name_1 Nullable(String),
    string_id_1 Nullable(UUID),
    string_val_1 Nullable(String),
    string_name_2 Nullable(String),
    string_id_2 Nullable(UUID),
    string_val_2 Nullable(String),
    string_name_3 Nullable(String),
    string_id_3 Nullable(UUID),
    string_val_3 Nullable(String),
    string_name_4 Nullable(String),
    string_id_4 Nullable(UUID),
    string_val_4 Nullable(String),
    string_name_5 Nullable(String),
    string_id_5 Nullable(UUID),
    string_val_5 Nullable(String),
    string_name_6 Nullable(String),
    string_id_6 Nullable(UUID),
    string_val_6 Nullable(String),
    string_name_7 Nullable(String),
    string_id_7 Nullable(UUID),
    string_val_7 Nullable(String),
    string_name_8 Nullable(String),
    string_id_8 Nullable(UUID),
    string_val_8 Nullable(String),
    string_name_9 Nullable(String),
    string_id_9 Nullable(UUID),
    string_val_9 Nullable(String),
    string_name_10 Nullable(String),
    string_id_10 Nullable(UUID),
    string_val_10 Nullable(String),
    string_name_11 Nullable(String),
    string_id_11 Nullable(UUID),
    string_val_11 Nullable(String),
    string_name_12 Nullable(String),
    string_id_12 Nullable(UUID),
    string_val_12 Nullable(String),
    string_name_13 Nullable(String),
    string_id_13 Nullable(UUID),
    string_val_13 Nullable(String),
    string_name_14 Nullable(String),
    string_id_14 Nullable(UUID),
    string_val_14 Nullable(String),
    string_name_15 Nullable(String),
    string_id_15 Nullable(UUID),
    string_val_15 Nullable(String)
) ENGINE = MergeTree()
ORDER BY (ts, id)
PRIMARY KEY (ts, id)
PARTITION BY (toMonth(ts))
