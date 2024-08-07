import json
from typing import Any, Type

from dataclasses import dataclass

from overture_song.validation import Validatable
from overture_song.utils import Builder, default_value
from typing import List
from dataclasses import is_dataclass, asdict
from overture_song.utils import check_type, check_state


class Entity(object):

    def to_json(self):

        return json.dumps(self.to_dict(), indent=4)

    def to_dict(self):
        if is_dataclass(self):
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
    dataType: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def create(cls, fileName, fileSize, fileType, fileMd5sum,
               fileAccess, studyId=None, analysisId=None, objectId=None, info=None, dataType=None):
        f = File()
        f.objectId = objectId
        f.analysisId = analysisId
        f.studyId = studyId
        f.fileType = fileType
        f.fileSize = fileSize
        f.info = default_value(info, {})
        f.fileMd5sum = fileMd5sum
        f.fileAccess = fileAccess
        f.fileName = fileName
        f.dataType = dataType
        return f


@dataclass(frozen=False)
class Sample(Metadata, Validatable):
    sampleId: str = None
    specimenId: str = None
    submitterSampleId: str = None
    sampleType: str = None
    matchedNormalSubmitterSampleId: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def create(cls, specimenId, submitterSampleId,
               sampleType, sampleId=None, info=None, matchedNormalSubmitterSampleId=None):
        s = Sample()
        s.info = default_value(info, {})
        s.specimenId = specimenId
        s.submitterSampleId = submitterSampleId
        s.sampleType = sampleType
        s.sampleId = sampleId
        s.matchedNormalSubmitterSampleId = matchedNormalSubmitterSampleId
        return s


@dataclass(frozen=False)
class Specimen(Metadata, Validatable):
    specimenId: str = None
    donorId: str = None
    submitterSpecimenId: str = None
    tumourNormalDesignation: str = None
    specimenType: str = None
    specimenTissueSource: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def create(cls, donorId, submitterSpecimenId, tumourNormalDesignation, specimenType,
               specimenId=None, info=None, specimenTissueSource=None):
        s = Specimen()
        s.info = default_value(info, {})
        s.specimenId = specimenId
        s.donorId = donorId
        s.specimenType = specimenType
        s.tumourNormalDesignation = tumourNormalDesignation
        s.submitterSpecimenId = submitterSpecimenId
        s.specimenTissueSource = specimenTissueSource
        return s


@dataclass(frozen=False)
class Donor(Metadata, Validatable):
    donorId: str = None
    submitterDonorId: str = None
    studyId: str = None
    gender: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def create(cls, submitterDonorId, studyId, gender, donorId=None, info=None):
        d = Donor()
        d.donorId = donorId
        d.info = default_value(info, {})
        d.studyId = studyId
        d.submitterDonorId = submitterDonorId
        d.gender = gender
        return d

@dataclass(frozen=False)
class AnalysisType(Validatable):
    name: str = None
    version: int = 1

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def create(cls, name, version = 1):
        t = AnalysisType()
        t.name = name
        t.version = version
        return t

@dataclass(frozen=False)
class CompositeEntity(Sample):
    specimen: Type[Specimen] = None
    donor: Type[Donor] = None

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def base_on_sample(cls, sample):
        s = CompositeEntity()
        s.sampleId = sample.sampleId
        s.sampleType = sample.sampleType
        s.info = sample.info
        s.specimenId = sample.specimenId
        s.submitterSampleId = sample.submitterSampleId
        s.matchedNormalSubmitterSampleId = sample.matchedNormalSubmitterSampleId

        return s

    @classmethod
    def create(cls, donor, specimen, sample):
        c = CompositeEntity.base_on_sample(sample)
        check_type(donor, Donor)
        check_type(specimen, Specimen)
        c.donor = donor
        c.specimen = specimen
        return c


@dataclass(frozen=False)
class Experiment(Metadata):
    pass


@dataclass(frozen=False)
class VariantCall(Experiment, Validatable):
    analysisId: str = None
    variantCallingTool: str = None
    matchedNormalSubmitterSampleId: str = None

    def validate(self):
        raise NotImplemented("not implemented")

    @classmethod
    def create(cls, variantCallingTool, matchedNormalSubmitterSampleId, analysisId=None):
        s = VariantCall()
        s.analysisId = analysisId
        s.variantCallingTool = variantCallingTool
        s.matchedNormalSubmitterSampleId = matchedNormalSubmitterSampleId
        return s


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

    @classmethod
    def create(cls, aligned, alignmentTool, insertSize,
               libraryStrategy, pairedEnd, referenceGenome, analysisId=None):
        s = SequencingRead()
        s.alignmentTool = alignmentTool
        s.aligned = aligned
        s.analysisId = analysisId
        s.libraryStrategy = libraryStrategy
        s.insertSize = insertSize
        s.pairedEnd = pairedEnd
        s.referenceGenome = referenceGenome
        return s


@dataclass(frozen=False)
class Analysis(Entity):
    analysisId: str = None
    studyId: str = None
    analysisState: str = "UNPUBLISHED"

    # TODO: add typing to this. should be a list of type Sample
    samples: List[CompositeEntity] = None

    # TODO: add typing to this. should be a list of type File
    files: List[File] = None

    def __post_init__(self):
        self.samples = []
        self.files = []

    @classmethod
    def builder(cls):
        return Builder(Analysis)

    @classmethod
    def from_json(cls, json_string):
        pass


@dataclass(frozen=False)
class SequencingReadAnalysis(Analysis, Validatable):
    analysisType: Type[AnalysisType] = AnalysisType.create("sequencingRead")

    # TODO: add typing to this. should be a list of type File
    experiment: Type[SequencingRead] = None

    @classmethod
    def create(cls, experiment, samples, files, analysisId=None, studyId=None, analysisState="UNPUBLISHED", info=None):
        check_type(experiment, SequencingRead)
        check_state(samples is not None and isinstance(samples, list) and len(samples) > 0,
                    "Atleast one sample must be defined")
        check_state(files is not None and isinstance(files, list) and len(files) > 0,
                    "Atleast one file must be defined")
        for s in samples:
            check_type(s, Sample)
        for f in files:
            check_type(f, File)

        s = SequencingReadAnalysis()
        s.samples = samples
        s.files = files
        s.info = default_value(info, {})
        s.experiment = experiment
        s.analysisId = analysisId
        s.studyId = studyId
        s.analysisState = analysisState
        return s

    def validate(self):
        raise NotImplemented("not implemented")


@dataclass(frozen=False)
class VariantCallAnalysis(Analysis, Validatable):
    analysisType: Type[AnalysisType] = AnalysisType.create("variantCall")

    # TODO: add typing to this. should be a list of type File
    experiment: Type[VariantCall] = None

    @classmethod
    def create(cls, experiment, samples, files, analysisId=None, studyId=None, analysisState="UNPUBLISHED", info=None):
        check_type(experiment, VariantCall)
        check_state(samples is not None and isinstance(samples, list) and len(samples) > 0,
                    "Atleast one sample must be defined")
        check_state(files is not None and isinstance(files, list) and len(files) > 0,
                    "Atleast one file must be defined")
        for s in samples:
            check_type(s, CompositeEntity)
        for f in files:
            check_type(f, File)

        s = VariantCallAnalysis()
        s.experiment = experiment
        s.analysisId = analysisId
        s.studyId = studyId
        s.analysisState = analysisState
        s.samples = samples
        s.files = files
        s.info = default_value(info, {})
        return s

    def validate(self):
        raise NotImplemented("not implemented")
