from abc import abstractmethod

from overture_song.utils import check_state, default_value, check_type
from collections import OrderedDict
import inspect


class Validatable(object):

    @abstractmethod
    def validate(self):
        pass


class DataField(object):
    def __init__(self, name, *types, required=True, multiple=False):
        self.types = types
        self.name = name
        self.required = required
        self.multiple = multiple

    def validate(self, value):
        if self.multiple:
            self._validate_single(list, value)
            if self.required:
                check_state(len(value) > 0,
                            "The required list datafield '{}' was supplied an empty array",
                            self.name)
            if value is not None:
                for t in self.types:
                    for item in value:
                        self._validate_single(t, item)
        else:
            for t in self.types:
                self._validate_single(t, value)

    def _validate_single(self, t, value):
        if self.required:
            check_state(value is not None, "The datafield '{}' is required", self.name)

        if value is not None:
            if self.required:
                if t is str:
                    check_state(value, "The required string datafield '{}' was supplied an empty string ",
                                self.name)
            check_state(isinstance(value, t),
                        "The datafield '{}' is of '{}' type, but was supplied a value of type '{}' with value '{}'",
                        self.name, t, type(value), value)


"""
    Validation decorator that intercepts "setting" of properties/attributes defined by the @dataclass decorator. 
    Once intercepted, it validates the input value to set against predefined rules, encapsulated in a DataField class
"""


# TODO: Update implementation to process dataclass definition. Should extract properties, their types, if they are a
# list or not, if they are required or optional
class validation(object):
    def __init__(self, *datafields):
        self.datafields = list(datafields)
        check_type(self.datafields, list)
        check_state(len(self.datafields) > 0, "Must define atleast one datafield")
        self.name_type_map = {}
        for datafield in self.datafields:
            check_type(datafield, DataField)
            if datafield.name not in self.name_type_map:
                self.name_type_map[datafield.name] = {}
            for t in datafield.types:
                t_name = t.__name__
                if t_name in self.name_type_map[datafield.name]:
                    raise Exception(
                        "Collision: The datafield definition '{}' already exists as '{}' for the type '{}'".format(
                            datafield, self.name_type_map[datafield.name][t_name], t_name))
                self.name_type_map[datafield.name][t_name] = datafield

    def __call__(self, Cls):
        name_type_map = self.name_type_map
        datafields = self.datafields

        class Validator(Cls):

            SPECIAL_FIELD = '__dataclass_fields__'
            _INTERNAL_DICT = Cls.__dict__[SPECIAL_FIELD]

            def __init__(self, *args, **kwargs):

                Cls.__init__(self, *args, **kwargs)
                check_state(Validator.SPECIAL_FIELD in Cls.__dict__,
                            "Decorator can only process dataclasses")
                Validator._check_validator()

            @classmethod
            def _check_validator(cls):
                available_fields = Validator._INTERNAL_DICT.keys()
                undefined_set = set()

                for d in datafields:
                    if d.name not in available_fields:
                        undefined_set.add(d.name)
                num_undefined = len(undefined_set)
                check_state(num_undefined == 0,
                            "The {} datafields [{}] do not exist in the class definition for '{}'",
                            num_undefined,
                            ",".join(undefined_set),
                            Cls.__name__)

            def __setattr__(self, datafield_name, value):
                if datafield_name in name_type_map:
                    for t, d in name_type_map[datafield_name].items():
                        d.validate(value)
                if datafield_name not in Validator._INTERNAL_DICT.keys():
                    raise AttributeError(
                        "The field '{}' is not an attribute of '{}'".format(datafield_name, Cls.__name__))
                object.__setattr__(self, datafield_name, value)

        return Validator


# [BUG] : cannot process functions that have @classmethod annotation
def non_null(exclude=None):
    exclude = default_value(exclude, [])

    def wrap(func):
        inspected_args = OrderedDict.fromkeys(inspect.signature(func).parameters.keys(), True)
        is_self = False
        if "self" in inspected_args.keys():
            is_self = True
            inspected_args.pop("self")

        def new_func(*args, **kwargs):
            count = 1 if is_self else 0
            for a in inspected_args.keys():
                if a not in exclude:
                    check_state(args[count] is not None, "The argument '{}' must be non-null", a)
                count += 1
            return func(*args, **kwargs)
        return new_func
    return wrap
