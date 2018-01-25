from abc import abstractmethod

from overture_song import utils
from overture_song.utils import check_state


class Validatable(object):
    @abstractmethod
    def validate(self):
        pass

    @classmethod
    def validate_string(cls, value: str):
        utils.check_type(value, str)

    @classmethod
    def validate_int(cls, value: int):
        utils.check_type(value, int)

    @classmethod
    def validate_float(cls, value: float):
        utils.check_type(value, float)

    @classmethod
    def validate_not_none(cls, value: float):
        check_state(value is not None, "The input value cannot be None")

    @classmethod
    def validate_string_not_empty_or_none(cls, value: str):
        Validatable.validate_string(value)
        Validatable.validate_not_none(value)
        check_state(value, "The input string value cannot be empty")

    @classmethod
    def validate_required_string(cls, value: str):
        Validatable.validate_string_not_empty_or_none(value)

    def required(cls, original_function):
        def new_function(value):
            out = original_function(value)
            check_state(out is not None, "The input value cannot be None")

        return new_function

    def string(cls, original_function):
        def new_function(value):
            out = original_function(value)
            check_state(out is not None, "The input value cannot be None")

        return new_function


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
        utils.check_type(self.datafields, list)
        check_state(len(self.datafields) > 0, "Must define atleast one datafield")
        self.name_type_map = {}
        for datafield in self.datafields:
            utils.check_type(datafield, DataField)
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
                self._check_validator()

            def _check_validator(self):
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
