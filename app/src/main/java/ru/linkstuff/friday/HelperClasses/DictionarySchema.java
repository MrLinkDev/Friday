package ru.linkstuff.friday.HelperClasses;

import android.provider.BaseColumns;

public class DictionarySchema{
    public static final class CommandsTable implements BaseColumns {
        public static final String NAME = "commands";
        public static final String COMMAND_ID = "id";
        public static final String COMMAND = "command";
    }

    public static abstract class AppsTable implements BaseColumns{
        public static final String NAME = "apps";
        public static final String APP_VARIANTS = "app_variants";
        public static final String PACKAGE = "package";
    }

    public static abstract class DevicesTable implements BaseColumns{
        public static final String NAME = "devices";
        public static final String DEVICE_ID = "id";
        public static final String DEVICE_VARIANTS = "device_variants";
    }

    public static abstract class ExtraArgsTable implements BaseColumns{
        public static final String NAME = "extra_args";
        public static final String EXTRA_ARG = "extra_arg";
    }

    public static abstract class ArgsOfChangeTable implements BaseColumns{
        public static final String NAME = "args_of_change";
        public static final String ARG_OF_CHANGE = "arg_of_change";
    }
}
