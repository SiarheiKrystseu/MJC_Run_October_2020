package com.epam.esm.service;

import com.epam.esm.dao.TagDao;
import com.epam.esm.exception.InvalidInputDataException;
import com.epam.esm.exception.TagNotFoundException;
import com.epam.esm.model.Tag;
import com.epam.esm.validator.TagValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

/**
 * @author Sergei Kristev
 * <p>
 * Service for managing Tag objects.
 */
@Service
public class TagServiceImpl implements TagService {

    private final TagDao tagDao;
    private final TagValidator tagValidator;

    /**
     * Constructor accepts TagDao object.
     *
     * @param tagDao        TagDao instance.
     * @param tagValidator  TagValidator instance.
     */
    @Autowired
    public TagServiceImpl(TagDao tagDao, TagValidator tagValidator) {
        this.tagDao = tagDao;
        this.tagValidator = tagValidator;
    }

    /**
     * Gets list of all tags from TagDao.
     *
     * @return Tags list.
     */
    @Override
    public List<Tag> findAllTags(int from, int pageSize) {

        List<Tag> tags = tagDao.findAll(from, pageSize);
        if (tags.isEmpty()) {
            throw new TagNotFoundException("Tags were not found");
        } else {
            return tags;
        }
    }

    /**
     * Gets tag by id.
     *
     * @param id Tag id.
     * @return Tag instance.
     */
    @Override
    public Tag findTagById(Long id) {
        return tagDao.find(id).orElseThrow(() -> new TagNotFoundException(MessageFormat
                .format("Tag with id: {0} not found", id)));
    }

    /**
     * Saves new tag.
     *
     * First, finds a tag by tag name. Subsequently, if the tag record is not exists, method saves tag through <i>tagDao</i>, else
     * throw IllegalArgumentException;
     *
     * @param tag Tag instance.
     * @return Tag id.
     * @throws IllegalArgumentException Tag with name: {0} already exists.
     */
    @Transactional
    @Override
    public Long saveTag(Tag tag) {
        Optional<Tag> tagOptional = tagDao.findByTagName(tag.getName());
        if (tagOptional.isPresent()) {
            throw new IllegalArgumentException(MessageFormat.format("Tag with name: {0} already exists", tag.getName()));
        }
        BindingResult result = new BeanPropertyBindingResult(tag, "tag");
        tagValidator.validate(tag, result);
        if (result.hasErrors()) {
            String brokenField = result.getFieldErrors().get(0).getField();
            String errorCode = result.getFieldErrors().get(0).getCode();
            throw new InvalidInputDataException(MessageFormat.format("Unexpected tag''s field: {0}, error code: {1}",
                    brokenField, errorCode));
        }
        return tagDao.save(tag);
    }

    /**
     * Updates tag.
     * <p>
     * Updating tag through <i>tagDao</i>.
     *
     * @param tag Tag instance.
     */
    @Transactional
    @Override
    public void updateTag(Tag tag) {
        BindingResult result = new BeanPropertyBindingResult(tag, "tag");
        tagValidator.validate(tag, result);
        if (result.hasErrors()) {
            String brokenField = result.getFieldErrors().get(0).getField();
            String errorCode = result.getFieldErrors().get(0).getCode();
            throw new InvalidInputDataException(MessageFormat.format("Unexpected tag''s field: {0}, error code: {1}",
                    brokenField, errorCode));
        } else {
            tagDao.update(tag);
        }
    }

    /**
     * Assigns tag.
     * <p>
     * Assigning tag to gift certificate through <i>tagDao</i>.
     *
     * @param tagId         Tag id.
     * @param certificateId GiftCertificate id.
     */
    @Override
    public void assignTag(Long tagId, Long certificateId) {
        tagDao.assignTag(tagId, certificateId);
    }

    /**
     * Deletes tag.
     * <p>
     * First, finds a tag by ID. Subsequently, if the tag record is exists, method deletes tag through <i>tagDao</i>, else
     * throw TagNotFoundException;
     *
     * @param id Tag id.
     * @throws TagNotFoundException Tag with id: {0} not found
     */
    @Transactional
    @Override
    public void deleteTag(Long id) {
        tagDao.find(id).orElseThrow(() ->
                new TagNotFoundException(MessageFormat.format("Tag with id: {0} not found", id)));
        tagDao.delete(id);
    }

    /**
     * Assigns default tag.
     * <p>
     * First, finds a tag by tag name. Subsequently, if the tag record is exists, method assigns default tag
     * through <i>tagDao</i>, else throw TagNotFoundException;
     *
     * @param tagName       Tag name.
     * @param certificateId Certificate id.
     * @throws TagNotFoundException The tag with tag name: {0} not found
     */
    @Transactional
    @Override
    public void assignDefaultTag(String tagName, Long certificateId) {
        Optional<Tag> tag = tagDao.findByTagName(tagName);
        if (!tag.isPresent()) {
            throw new TagNotFoundException(MessageFormat.format("The tag with tag name: {0} not found", tagName));
        } else {
            Tag currentTag = tag.get();
            tagDao.assignTag(currentTag.getId(), certificateId);
        }
    }

    /**
     * Assigns new tag to certificate.
     * <p>
     * First, finds a tag by tag name. Subsequently, if the tag record is exists, method assigns passed tag
     * through <i>tagDao</i>, else throw TagNotFoundException;
     *
     * @param tagName       Tag name.
     * @param certificateId Certificate id.
     * @throws TagNotFoundException The tag with tag name: {0} not found
     */
    @Transactional
    @Override
    public void addNewTagAndCertificate(String tagName, Long certificateId) {
        Optional<Tag> tag = tagDao.findByTagName(tagName);
        if (!tag.isPresent()) {
            throw new TagNotFoundException(MessageFormat.format("The tag with tag name: {0} not found", tagName));
        } else {
            Tag currentTag = tag.get();
            tagDao.addNewTagToCertificate(currentTag.getId(), certificateId);
        }
    }

    /**
     * Finds tag by tag name.
     * <p>
     * Finds a tag by tag name, else throw TagNotFoundException;
     *
     * @param tagName Tag name.
     * @throws TagNotFoundException The tag with tag name: {0} not found
     */
    @Override
    public Tag findTagByTagName(String tagName) {
        Optional<Tag> tag = tagDao.findByTagName(tagName);
        if (!tag.isPresent()) {
            throw new TagNotFoundException(MessageFormat.format("The tag with tag name: {0} not found", tagName));
        } else {
            return tag.get();
        }
    }

    /**
     * Updates tag list.
     * <p>
     * The method accepts list with tags and certificate's ID. After then every tag validates, a tag-certificate
     * relationship is assigned. If the passed tag is not present in the DB, we create it.
     *
     * @param tags              List with tags.
     * @param certificateId     GiftCertificate's ID.
     */
    @Transactional
    @Override
    public void updateTagList(List<Tag> tags, Long certificateId) {

        for (Tag tag : tags) {
            BindingResult result = new BeanPropertyBindingResult(tag, "tag");
            TagValidator validator = new TagValidator();
            validator.validate(tag, result);
            if (result.hasErrors()) {
                String brokenField = result.getFieldErrors().get(0).getField();
                String errorCode = result.getFieldErrors().get(0).getCode();
                throw new InvalidInputDataException(MessageFormat.format("Unexpected tag''s field: {0}, error code: {1}",
                        brokenField, errorCode));
            }
            Optional<Tag> currentTag = tagDao.findByTagName(tag.getName());
            if (currentTag.isPresent()) {
                tagDao.addNewTagToCertificate(currentTag.get().getId(), certificateId);
            } else {
                Long tagId = tagDao.save(tag);
                tagDao.addNewTagToCertificate(tagId, certificateId);
            }
        }
    }
}
