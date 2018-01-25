import json
from typing import Any, Type

from dataclasses import dataclass, field

from overture_song import utils
from overture_song.validation import Validatable
from overture_song.utils import Builder
from abc import abstractmethod
from typing import List


class Entity(object):

    def to_json(self):
        return json.dumps(self.to_dict(), indent=4)

    @abstractmethod
    def to_dict(self):
        pass

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

    def to_dict(self):
        return { "info" : self.info }

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


    @classmethod
    def create_from_raw(cls, study_obj):
        return Study.create(
            study_obj.studyId,
            name=study_obj.name,
            description=study_obj.description,
            organization=study_obj.organization)

    def to_dict(self):
        this_dict = {
            "studyId" : self.studyId,
            "name" : self.name,
            "organization" : self.organization,
            "description" : self.description
        }
        this_dict.update(super().to_dict())
        return this_dict



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

    def to_dict(self):
        this_dict = {
            "objectId": self.objectId,
            "analysisId": self.analysisId,
            "fileName": self.fileName,
            "studyId": self.studyId,
            "fileSize": self.fileSize,
            "fileType": self.fileType,
            "fileMd5sum": self.fileMd5sum,
            "fileAccess": self.fileAccess,
        }
        this_dict.update(super().to_dict())
        return this_dict


@dataclass(frozen=False)
class Sample(Metadata, Validatable):
    sampleId: str = None
    specimenId: str = None
    sampleSubmitterId: str = None
    sampleType: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    def to_dict(self):
        info_dict = super().to_dict()
        this_dict = {
            "sampleId" : self.sampleId,
            "specimenId" : self.specimenId,
            "sampleSubmitterId" : self.sampleSubmitterId,
            "sampleType" : self.sampleType,
        }
        this_dict.update(info_dict)
        return this_dict


@dataclass(frozen=False)
class Specimen(Metadata, Validatable):
    specimenId: str = None
    donorId: str = None
    specimenSubmitterId: str = None
    specimenClass: str = None
    specimenType: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    def to_dict(self):
        info_dict = super().to_dict()
        this_dict = {
            "specimenId" : self.specimenId,
            "donorId" : self.donorId,
            "specimenSubmitterId" : self.specimenSubmitterId,
            "specimenClass" : self.specimenClass,
            "specimenType" : self.specimenType,
        }
        this_dict.update(info_dict)
        return this_dict


@dataclass(frozen=False)
class Donor(Metadata, Validatable):
    donorId: str = None
    donorSubmitterId: str = None
    studyId: str = None
    donorGender: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    def to_dict(self):
        info_dict = super().to_dict()
        this_dict = {
            "donorId" : self.donorId,
            "donorSubmitterId" : self.donorSubmitterId,
            "studyId" : self.studyId,
            "donorGender" : self.donorGender,
        }
        this_dict.update(info_dict)
        return this_dict



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

    def to_dict(self):
        this_dict = {}
        this_dict.update(super().to_dict())
        this_dict["specimen"] = self.specimen.to_dict()
        this_dict["donor"] = self.donor.to_dict()
        return this_dict



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

    def to_dict(self):
        out = {
            "analysisId" : self.analysisId,
            "variantCallingTool" : self.variantCallingTool,
            "matchedNormalSampleSubmitterId" : self.matchedNormalSampleSubmitterId,
        }
        out.update(super().to_dict())
        return out


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

    def to_dict(self):
        out = {
            "analysisId" : self.analysisId,
            "aligned" : self.aligned,
            "alignmentTool" : self.alignmentTool,
            "insertSize" : self.insertSize,
            "libraryStrategy" : self.libraryStrategy,
            "pairedEnd" : self.pairedEnd,
            "referenceGenome" : self.referenceGenome,
        }
        out.update(super().to_dict())
        return out



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

    def to_dict(self):
        return {
            "analysisId": self.analysisId,
            "study": self.study,
            "analysisState": self.analysisState,
            "sample": list(map(lambda s: s.to_dict(), self.sample)),
            "file": list(map(lambda f: f.to_dict(), self.file))
        }


@dataclass(frozen=False)
class SequencingReadAnalysis(Analysis):
    analysisType: str = "sequencingRead"

    # TODO: add typing to this. should be a list of type File
    experiment: Type[SequencingRead] = None

    def to_dict(self):
        out = {
            "experiment": self.experiment.to_dict(),
            "analysisType" : self.analysisType
        }
        out.update(super().to_dict())
        return out


@dataclass(frozen=False)
class VariantCallAnalysis(Analysis):
    analysisType: str = 'variantCall'

    # TODO: add typing to this. should be a list of type File
    experiment: Type[VariantCall] = None

    def to_dict(self):
        out = {
            "experiment": self.experiment.to_dict(),
            "analysisType" : self.analysisType,
        }
        out.update(super().to_dict())
        return out
