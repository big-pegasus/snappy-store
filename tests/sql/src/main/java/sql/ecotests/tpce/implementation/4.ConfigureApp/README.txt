1. t_id in Trade table has to been defined as "generated by default as identity"
when external data (with existing t_id) is migrated to GemFireXD. After migration,
the id start number should be bump to the max t_id + 1, otherwise, a new t_id returned
will start at 1, which will violate the primary key constraint in trade table.

2. gfxd interactive command has limited control over its output, so we need some special way
to extract the desired result from its output in script (see reset_tradeid.sh)
