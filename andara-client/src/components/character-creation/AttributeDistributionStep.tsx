import React, { useState, useEffect } from 'react';
import { useAppSelector, useAppDispatch } from '../../store/hooks';
import {
  setStep,
  updateFormData,
  setValidationErrors,
} from '../../store/slices/characterCreationSlice';
import type { Attributes } from '../../types/character';

const MIN_ATTRIBUTE = 6;
const MAX_ATTRIBUTE = 16;
const BASE_ATTRIBUTE = 8;
const POINTS_TO_DISTRIBUTE = 27;

export const AttributeDistributionStep: React.FC = () => {
  const dispatch = useAppDispatch();
  const { formData, validationErrors } = useAppSelector(
    (state) => state.characterCreation
  );

  const [attributes, setAttributes] = useState<Attributes>(
    formData.attributes || {
      strength: BASE_ATTRIBUTE,
      agility: BASE_ATTRIBUTE,
      endurance: BASE_ATTRIBUTE,
      intellect: BASE_ATTRIBUTE,
      perception: BASE_ATTRIBUTE,
      charisma: BASE_ATTRIBUTE,
    }
  );

  const [pointsRemaining, setPointsRemaining] = useState(POINTS_TO_DISTRIBUTE);

  useEffect(() => {
    const total = Object.values(attributes).reduce((sum, val) => sum + val, 0);
    const baseTotal = BASE_ATTRIBUTE * 6;
    const used = total - baseTotal;
    setPointsRemaining(POINTS_TO_DISTRIBUTE - used);
  }, [attributes]);

  const updateAttribute = (
    attr: keyof Attributes,
    delta: number
  ) => {
    const current = attributes[attr];
    const newValue = Math.max(
      MIN_ATTRIBUTE,
      Math.min(MAX_ATTRIBUTE, current + delta)
    );
    setAttributes({ ...attributes, [attr]: newValue });
  };

  const handleNext = () => {
    const total = Object.values(attributes).reduce((sum, val) => sum + val, 0);
    const baseTotal = BASE_ATTRIBUTE * 6;
    const used = total - baseTotal;

    if (used < 0 || used > POINTS_TO_DISTRIBUTE) {
      dispatch(
        setValidationErrors({
          attributes: 'Invalid attribute distribution',
        })
      );
      return;
    }

    dispatch(updateFormData({ attributes }));
    dispatch(setStep('skills'));
  };

  const attributeLabels: Record<keyof Attributes, string> = {
    strength: 'Strength',
    agility: 'Agility',
    endurance: 'Endurance',
    intellect: 'Intellect',
    perception: 'Perception',
    charisma: 'Charisma',
  };

  return (
    <div className="attribute-distribution-step">
      <h2>Distribute Attributes</h2>
      <p>
        You have {POINTS_TO_DISTRIBUTE} points to distribute. Each attribute
        must be between {MIN_ATTRIBUTE} and {MAX_ATTRIBUTE}.
      </p>

      <div className="points-remaining">
        Points Remaining: <strong>{pointsRemaining}</strong>
      </div>

      {validationErrors.attributes && (
        <div className="error">{validationErrors.attributes}</div>
      )}

      <div className="attribute-list">
        {(Object.keys(attributes) as Array<keyof Attributes>).map((attr) => (
          <div key={attr} className="attribute-row">
            <label>{attributeLabels[attr]}</label>
            <div className="attribute-controls">
              <button
                onClick={() => updateAttribute(attr, -1)}
                disabled={attributes[attr] <= MIN_ATTRIBUTE}
              >
                -
              </button>
              <span className="attribute-value">{attributes[attr]}</span>
              <button
                onClick={() => updateAttribute(attr, 1)}
                disabled={
                  attributes[attr] >= MAX_ATTRIBUTE || pointsRemaining <= 0
                }
              >
                +
              </button>
            </div>
          </div>
        ))}
      </div>

      <div className="step-actions">
        <button onClick={() => dispatch(setStep('origin'))}>Back</button>
        <button onClick={handleNext}>Next: Skills</button>
      </div>
    </div>
  );
};

