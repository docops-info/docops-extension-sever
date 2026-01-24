# DocOps Planner

<div style="background: white; border: 2px solid #e2e8f0; border-radius: 12px; padding: 32px; margin-bottom: 48px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);">
  <div style="display: flex; align-items: center; gap: 24px;">
    <div style="background: linear-gradient(135deg, #ea580c 0%, #fb923c 100%); padding: 20px; border-radius: 12px;">
      <img src="../images/PlannerIcon.svg" alt="Planner Icon" width="80" height="80" />
    </div>
    <div>
      <h1 style="margin: 0 0 12px 0; color: #c2410c; font-size: 32px;">DocOps Planner</h1>
      <p style="margin: 0; color: #64748b; font-size: 16px;">Turn plans into clear, visual task snapshots for teams and stakeholders</p>
    </div>
  </div>
</div>

[TOC]

## What is DocOps Planner?

DocOps Planner transforms simple, indented task lists into structured planning visuals. Use it to outline initiatives, track progress across phases, and communicate dependencies with a clean, shareable layout.

### Key Features

- **Phase-based planning** - Organize tasks by status buckets
- **Readable snapshots** - Great for updates, reviews, and reports
- **Simple input** - Write plans with a lightweight list format
- **Progress visibility** - Show what is now, next, later, and done

---

## Default Look

[docops:planner]
- now Market Research
  Conduct competitor analysis
  Identify target audience
  Define unique selling points
- next Product Development
  Create prototype
  Test with focus groups
  Refine based on feedback
- later Marketing Campaign
  Develop marketing materials
  Plan social media strategy
  Prepare press releases
- done Business Plan
  Define business model
  Secure initial funding
  Assemble core team

[/docops]

---

## Release Planning Example

[docops:planner]
- now Beta Readiness
  Finish onboarding flow
  Lock pricing tiers
  Run security review
- next GA Launch
  Publish documentation
  Train support team
  Enable billing
- later Growth Experiments
  Launch referral program
  Test onboarding variants
  Expand integrations
- done Foundations
  Set up CI pipeline
  Establish release checklist
  Instrument core metrics
[/docops]

---

## Format Options

### Planner Structure

Use a list of phases with indented tasks beneath each phase:

```text
[docops:planner]
- now Market Research
  Conduct competitor analysis
  Identify target audience
  Define unique selling points
- next Product Development
  Create prototype
  Test with focus groups
  Refine based on feedback
- later Marketing Campaign
  Develop marketing materials
  Plan social media strategy
  Prepare press releases
- done Business Plan
  Define business model
  Secure initial funding
  Assemble core team
[/docops]
```

### Common Phases

- **now** - In progress
- **next** - Up next
- **later** - Backlog or future work
- **done** - Completed items

---

## Best Practices

- **Keep phases consistent** - Use the same buckets across plans
- **Limit tasks per phase** - Prevent clutter and keep focus
- **Use action verbs** - Make tasks easy to scan
- **Update frequently** - Keep status credible

<div style="background: #fff7ed; border-left: 4px solid #fb923c; padding: 16px 24px; margin: 32px 0; border-radius: 4px;">
  <p style="margin: 0; color: #c2410c; font-weight: 600;">🗓 Planning Tip</p>
  <p style="margin: 8px 0 0 0; color: #475569;">Limit each phase to the work you can explain in one meeting. If it needs more detail, link to a deeper plan.</p>
</div>

---

## Common Use Cases

- **Quarterly planning** - Align teams on priorities
- **Project kickoffs** - Establish phases and ownership
- **Product launches** - Track readiness across functions
- **Operational roadmaps** - Communicate infrastructure work
- **Leadership updates** - Provide quick status snapshots
