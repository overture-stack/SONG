import json
from typing import Any, Type

from dataclasses import dataclass

from overture_song.validation import Validatable
from overture_song.utils import Builder
from typing import List
from dataclasses import _isdataclass, asdict


class Entity(object):

    def to_json(self):
        return json.dumps(self.to_dict(), indent=4)

    def to_dict(self):
        if _isdataclass(self):
            return asdict(self)
        else:
            raise NotImplemented("not implemented for non-dataclass object")

    def __str__(self):
        return self.to_json()


@dataclass(frozen=False)
class Metadata(Entity):
    info: dict = None

    def __post_init__(self):
        self.info = {}

    def set_info(self, key: str, value: Any):
        self.info[key] = value

    def add_info(self, data: dict):
        if data is None:
            return
        self.info.update(data)


@dataclass(frozen=False)
class Study(Metadata, Validatable):
    studyId: str = None
    name: str = None
    organization: str = None
    description: str = None

    def validate(self):
        raise NotImplemented("not implemented")


    @classmethod
    def create(cls, studyId, name=None, description=None, organization=None):
        s = Study()
        s.studyId = studyId
        s.name = name
        s.description = description
        s.organization = organization
        return s


    @classmethod
    def create_from_raw(cls, study_obj):
        return Study.create(
            study_obj.studyId,
            name=study_obj.name,
            description=study_obj.description,
            organization=study_obj.organization)


@dataclass(frozen=False)
class File(Metadata, Validatable):
    objectId: str = None
    analysisId: str = None
    fileName: str = None
    studyId: str = None
    fileSize: int = -1
    fileType: str = None
    fileMd5sum: str = None
    fileAccess: str = None

    def validate(self):
        raise NotImplemented("not implemented")


@dataclass(frozen=False)
class Sample(Metadata, Validatable):
    sampleId: str = None
    specimenId: str = None
    sampleSubmitterId: str = None
    sampleType: str = None

    def validate(self):
        raise NotImplemented("not implemented")


@dataclass(frozen=False)
class Specimen(Metadata, Validatable):
    specimenId: str = None
    donorId: str = None
    specimenSubmitterId: str = None
    specimenClass: str = None
    specimenType: str = None

    def validate(self):
        raise NotImplemented("not implemented")


@dataclass(frozen=False)
class Donor(Metadata, Validatable):
    donorId: str = None
    donorSubmitterId: str = None
    studyId: str = None
    donorGender: str = None

    def validate(self):
        raise NotImplemented("not implemented")


@dataclass(frozen=False)
class CompositeEntity(Sample):
    specimen: Type[Specimen] = None
    donor: Type[Donor] = None

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def create_from_sample(cls, sample):
        s = CompositeEntity()
        s.sampleId = sample.sampleId
        s.sampleSubmitterId = sample.sampleSubmitterId
        s.sampleType = sample.sampleType
        s.info = sample.info
        s.specimenId = sample.specimenId
        return s


@dataclass(frozen=False)
class Experiment(Metadata):
    pass


@dataclass(frozen=False)
class VariantCall(Experiment, Validatable):
    analysisId: str = None
    variantCallingTool: str = None
    matchedNormalSampleSubmitterId: str = None

    def validate(self):
        raise NotImplemented("not implemented")


@dataclass(frozen=False)
class SequencingRead(Experiment, Validatable):
    analysisId: str = None
    aligned: bool = None
    alignmentTool: str = None
    insertSize: int = None
    libraryStrategy: str = None
    pairedEnd: bool = None
    referenceGenome: str = None

    @classmethod
    def builder(cls):
        return Builder(Analysis)

    def validate(self):
        raise NotImplemented("not implemented")


# @validation(
#     DataField("analysisId", str),
#     DataField("file", str, multiple=True))
@dataclass(frozen=False)
class Analysis(Entity, Validatable):
    analysisId: str = None
    study: str = None
    analysisState: str = None

    # TODO: add typing to this. should be a list of type Sample
    sample: List[CompositeEntity] = None

    # TODO: add typing to this. should be a list of type File
    file: List[File] = None

    def __post_init__(self):
        self.sample = []
        self.file = []

    @classmethod
    def builder(cls):
        return Builder(Analysis)

    @classmethod
    def from_json(cls, json_string):
        pass

    def validate(self):
        raise NotImplemented("not implemented")


@dataclass(frozen=False)
class SequencingReadAnalysis(Analysis):
    analysisType: str = "sequencingRead"

    # TODO: add typing to this. should be a list of type File
    experiment: Type[SequencingRead] = None


@dataclass(frozen=False)
class VariantCallAnalysis(Analysis):
    analysisType: str = 'variantCall'

    # TODO: add typing to this. should be a list of type File
    experiment: Type[VariantCall] = None

